package com.example.testing1.data.remote

import co.touchlab.kermit.Logger
import com.powersync.PowerSyncDatabase
import com.powersync.connectors.PowerSyncBackendConnector
import com.powersync.connectors.PowerSyncCredentials
import com.powersync.db.crud.CrudEntry
import com.powersync.db.crud.CrudTransaction
import com.powersync.db.crud.UpdateType
import com.powersync.db.runWrapped
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.BucketApi
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.statement.bodyAsText
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

/**
 * Get a Supabase token to authenticate against the PowerSync instance.
 */
@OptIn(SupabaseInternal::class, InternalAPI::class)
open class SupabaseConnector(
    val supabaseClient: SupabaseClient,
    val powerSyncEndpoint: String,
    private val storageBucket: String? = null,
) : PowerSyncBackendConnector() {
    private val json = Json { coerceInputValues = true }
    private var errorCode: String? = null

    companion object PostgresFatalCodes {
        private val FATAL_RESPONSE_CODES =
            listOf(
                "^22...".toRegex(),
                "^23...".toRegex(),
                "^42501$".toRegex(),
            )

        fun isFatalError(code: String): Boolean =
            FATAL_RESPONSE_CODES.any { pattern ->
                pattern.matches(code)
            }
    }

    fun storageBucket(): BucketApi {
        if (storageBucket == null) {
            throw Exception("No bucket has been specified")
        }
        return supabaseClient.storage[storageBucket]
    }

    constructor(
        supabaseUrl: String,
        supabaseKey: String,
        powerSyncEndpoint: String,
        storageBucket: String? = null,
    ) : this(
        supabaseClient =
            createSupabaseClient(supabaseUrl, supabaseKey) {
                install(Auth)
                install(Postgrest)
                if (storageBucket != null) {
                    install(Storage)
                }
            },
        powerSyncEndpoint = powerSyncEndpoint,
        storageBucket = storageBucket,
    )

    init {
        require(supabaseClient.pluginManager.getPluginOrNull(Auth) != null) { "The Auth plugin must be installed on the Supabase client" }
        require(
            supabaseClient.pluginManager.getPluginOrNull(Postgrest) != null,
        ) { "The Postgrest plugin must be installed on the Supabase client" }

        supabaseClient.httpClient.httpClient.plugin(HttpSend)
            .intercept { request ->
                val resp = execute(request)
                val response = resp.response
                if (response.status.value >= 400) {
                    val responseText = response.bodyAsText()

                    try {
                        val error =
                            json.decodeFromString<Map<String, String?>>(
                                responseText,
                            )
                        errorCode = error["code"]
                    } catch (e: Exception) {
                        Logger.e("Failed to parse error response: $e")
                    }
                }
                resp
            }
    }

    suspend fun login(
        email: String,
        password: String,
    ) {
        runWrapped {
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        }
    }

    suspend fun signUp(
        email: String,
        password: String,
    ) {
        runWrapped {
            supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
        }
    }

    suspend fun signOut() {
        runWrapped {
            supabaseClient.auth.signOut()
        }
    }

    fun session(): UserSession? =
        supabaseClient.auth.currentSessionOrNull()

    val sessionStatus: StateFlow<SessionStatus> =
        supabaseClient.auth.sessionStatus

    suspend fun loginAnonymously() {
        runWrapped {
            supabaseClient.auth.signInAnonymously()
        }
    }

    override suspend fun fetchCredentials(): PowerSyncCredentials =
        runWrapped {
            check(supabaseClient.auth.sessionStatus.value is SessionStatus.Authenticated) { "Supabase client is not authenticated" }

            val session =
                supabaseClient.auth.currentSessionOrNull()
                    ?: error("Could not fetch Supabase credentials")

            check(session.user != null) { "No user data" }

            PowerSyncCredentials(
                endpoint = powerSyncEndpoint,
                token = session.accessToken,
            )
        }

    open suspend fun uploadCrudEntry(entry: CrudEntry) {
        val table = supabaseClient.from(entry.table)

        when (entry.op) {
            UpdateType.PUT -> {
                val data =
                    buildMap {
                        put("id", JsonPrimitive(entry.id))
                        entry.opData?.jsonValues?.let { putAll(it) }
                    }
                table.upsert(data)
            }

            UpdateType.PATCH -> {
                table.update(entry.opData!!.jsonValues) {
                    filter {
                        eq("id", entry.id)
                    }
                }
            }

            UpdateType.DELETE -> {
                table.delete {
                    filter {
                        eq("id", entry.id)
                    }
                }
            }
        }
    }

    open suspend fun handleError(
        tx: CrudTransaction,
        entry: CrudEntry,
        exception: Exception,
        errorCode: String?,
    ) {
        if (errorCode != null && isFatalError(errorCode)) {
            Logger.e("Data upload error: ${exception.message}")
            Logger.e("Discarding entry: $entry")
            tx.complete(null)
            return
        }

        Logger.e("Data upload error - retrying last entry: $entry, $exception")
        throw exception
    }

    override suspend fun uploadData(database: PowerSyncDatabase) {
        return runWrapped {
            val transaction =
                database.getNextCrudTransaction() ?: return@runWrapped

            var lastEntry: CrudEntry? = null
            try {
                for (entry in transaction.crud) {
                    lastEntry = entry
                    uploadCrudEntry(entry)
                }

                transaction.complete(null)
            } catch (e: Exception) {
                if (lastEntry != null) {
                    handleError(transaction, lastEntry, e, errorCode)
                } else {
                    throw e
                }
            }
        }
    }
}
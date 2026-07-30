package com.example.testing1.data.repository

import android.util.Log
import com.example.testing1.data.local.user.UserEntity
import com.powersync.PowerSyncDatabase
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val powerSyncDatabase: PowerSyncDatabase
) {
    val sessionStatus: StateFlow<SessionStatus> = supabaseClient.auth.sessionStatus

    val currentUserIdFlow: Flow<String> = sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> status.session.user?.id ?: "00000000-0000-0000-0000-000000000000"
            else -> "00000000-0000-0000-0000-000000000000"
        }
    }

    val currentUserId: String
        get() = (sessionStatus.value as? SessionStatus.Authenticated)?.session?.user?.id
            ?: "00000000-0000-0000-0000-000000000000"

    init {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            sessionStatus.collect { status ->
                if (status is SessionStatus.Authenticated) {
                    try {
                        Log.d("AuthRepository", "Session status Authenticated for user ${status.session.user?.id}")
                        persistCurrentUser()
                    } catch (e: Exception) {
                        Log.e("AuthRepository", "Auto-persist user profile failed: ${e.message}")
                    }
                }
            }
        }
    }

    suspend fun signUp(email: String, password: String, name: String) {
        Log.d("AuthRepository", "Attempting signUp for $email")
        try {
            supabaseClient.auth.signUpWith(
                Email,
                redirectUrl = "io.supabase.coffeeapp://login"
            ) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("full_name", name)
                }
            }
            Log.d("AuthRepository", "signUp call successful")
            persistCurrentUser(rawName = name, rawEmail = email)
        } catch (e: Exception) {
            Log.e("AuthRepository", "signUp call failed: ${e.message}", e)
            throw e
        }
    }

    suspend fun signIn(email: String, password: String) {
        Log.d("AuthRepository", "Attempting signIn for $email")
        try {
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Log.d("AuthRepository", "signIn call successful")
            persistCurrentUser(rawEmail = email)
        } catch (e: Exception) {
            Log.e("AuthRepository", "signIn call failed: ${e.message}", e)
            throw e
        }
    }

    suspend fun signInWithIdToken(
        idToken: String,
        nonce: String? = null,
        rawName: String? = null,
        rawEmail: String? = null,
        rawAvatarUrl: String? = null
    ) {
        val jwtClaims = parseJwtClaims(idToken)
        Log.d("AuthRepository", "Parsed JWT Claims: $jwtClaims")

        supabaseClient.auth.signInWith(IDToken) {
            this.idToken = idToken
            this.nonce = nonce
            provider = Google
        }

        // Extract email from Supabase identity data as fallback
        val identityEmail = extractEmailFromIdentities()

        val resolvedEmail = rawEmail
            ?: jwtClaims["email"]
            ?: identityEmail

        Log.d("AuthRepository", "Resolved email: $resolvedEmail")

        persistCurrentUser(
            rawName = rawName,
            rawEmail = resolvedEmail,
            rawAvatarUrl = rawAvatarUrl,
            jwtClaims = jwtClaims
        )
    }

    suspend fun signOut() {
        supabaseClient.auth.signOut()
    }

    /**
     * Extract email from Supabase user identities after sign-in.
     * When Supabase processes the Google ID token server-side, it may have the email
     * in the identity_data even when the client-side JWT doesn't contain it.
     */
    private fun extractEmailFromIdentities(): String? {
        return try {
            val user = (supabaseClient.auth.sessionStatus.value as? SessionStatus.Authenticated)
                ?.session?.user ?: return null

            // Check user.email first (Supabase may have resolved it server-side)
            val directEmail = user.email?.takeIf { it.contains("@") }
            if (directEmail != null) {
                Log.d("AuthRepository", "[EMAIL_EXTRACT] Found email directly on Supabase user: $directEmail")
                return directEmail
            }

            // Check identities for email in identity_data
            val identities = user.identities
            Log.d("AuthRepository", "[EMAIL_EXTRACT] User has ${identities?.size ?: 0} identities")
            identities?.forEach { identity ->
                val identityData = identity.identityData
                Log.d("AuthRepository", "[EMAIL_EXTRACT] Identity provider=${identity.provider}, identityData keys=${identityData?.keys}")
                val email = identityData?.get("email")?.jsonPrimitive?.content?.takeIf { it.contains("@") }
                if (email != null) {
                    Log.d("AuthRepository", "[EMAIL_EXTRACT] Found email in identity_data: $email")
                    return email
                }
            }

            // Check the Supabase access token JWT (it may contain the email)
            val accessToken = (supabaseClient.auth.sessionStatus.value as? SessionStatus.Authenticated)
                ?.session?.accessToken
            if (accessToken != null) {
                val accessClaims = parseJwtClaims(accessToken)
                val accessEmail = accessClaims["email"]?.takeIf { it.contains("@") }
                if (accessEmail != null) {
                    Log.d("AuthRepository", "[EMAIL_EXTRACT] Found email in Supabase access token: $accessEmail")
                    return accessEmail
                }
            }

            Log.w("AuthRepository", "[EMAIL_EXTRACT] No email found in any identity source")
            null
        } catch (e: Exception) {
            Log.e("AuthRepository", "[EMAIL_EXTRACT] Error extracting email from identities: ${e.message}")
            null
        }
    }

    private fun parseJwtClaims(token: String): Map<String, String> {
        return try {
            val parts = token.split(".")
            if (parts.size >= 2) {
                val payloadBase64 = parts[1]
                val decodedBytes = android.util.Base64.decode(
                    payloadBase64,
                    android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING or android.util.Base64.NO_WRAP
                )
                val payloadJson = String(decodedBytes, Charsets.UTF_8)
                val jsonElement = kotlinx.serialization.json.Json.parseToJsonElement(payloadJson)
                if (jsonElement is JsonObject) {
                    val map = mutableMapOf<String, String>()
                    jsonElement["email"]?.jsonPrimitive?.content?.let { if (it.isNotBlank()) map["email"] = it }
                    jsonElement["name"]?.jsonPrimitive?.content?.let { if (it.isNotBlank()) map["name"] = it }
                    jsonElement["picture"]?.jsonPrimitive?.content?.let { if (it.isNotBlank()) map["picture"] = it }
                    jsonElement["given_name"]?.jsonPrimitive?.content?.let { if (it.isNotBlank()) map["given_name"] = it }
                    jsonElement["family_name"]?.jsonPrimitive?.content?.let { if (it.isNotBlank()) map["family_name"] = it }
                    map
                } else emptyMap()
            } else emptyMap()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to parse JWT claims: ${e.message}", e)
            emptyMap()
        }
    }

    private suspend fun persistCurrentUser(
        rawName: String? = null,
        rawEmail: String? = null,
        rawAvatarUrl: String? = null,
        jwtClaims: Map<String, String> = emptyMap()
    ) {
        Log.d("AuthRepository", "[PIPELINE_STEP_1] persistCurrentUser() triggered. rawName='$rawName', rawEmail='$rawEmail', jwtClaims=$jwtClaims")
        
        // Wait for session to be fully ready
        val session = if (supabaseClient.auth.sessionStatus.value is SessionStatus.Authenticated) {
            (supabaseClient.auth.sessionStatus.value as SessionStatus.Authenticated).session
        } else {
            Log.d("AuthRepository", "[PIPELINE_STEP_2] Waiting for Authenticated status...")
            supabaseClient.auth.sessionStatus.filter { it is SessionStatus.Authenticated }.first().let {
                (it as SessionStatus.Authenticated).session
            }
        }
        
        val user = session.user ?: run {
            Log.e("AuthRepository", "[PIPELINE_ERROR] Session user is null!")
            return
        }
        
        Log.d("AuthRepository", "[PIPELINE_STEP_3] Supabase User ID: ${user.id}, User Email: ${user.email}")
        val metadata = user.userMetadata ?: buildJsonObject { }
        Log.d("AuthRepository", "[PIPELINE_STEP_3] User Metadata JSON: $metadata")

        // Read existing user row from local DB first to prevent overwriting valid data with placeholders
        val existingUser: UserEntity? = try {
            powerSyncDatabase.getOptional(
                "SELECT * FROM users WHERE id = ?",
                listOf(user.id)
            ) { cursor ->
                val cols = cursor.columnNames
                UserEntity(
                    id = cursor.getString(cols["id"]!!) ?: "",
                    name = cursor.getString(cols["name"]!!) ?: "",
                    email = cursor.getString(cols["email"]!!) ?: "",
                    avatarUrl = cursor.getString(cols["avatar_url"]!!)
                )
            }
        } catch (e: Exception) {
            Log.w("AuthRepository", "[PIPELINE_WARN] Could not query existing user from DB: ${e.message}")
            null
        }

        Log.d("AuthRepository", "[PIPELINE_STEP_4] Existing DB User record: $existingUser")
        
        // Priority order for email: user.email -> jwtClaims["email"] -> metadata -> rawEmail (must contain @)
        val candidateRawEmail = rawEmail?.takeIf { it.contains("@") }
        val rawMetaEmail = metadata["email"]?.jsonPrimitive?.content?.takeIf { it.contains("@") }
        val rawJwtEmail = jwtClaims["email"]?.takeIf { it.contains("@") }
        val rawUserEmail = user.email?.takeIf { it.contains("@") }

        val computedEmail = rawUserEmail
            ?: rawJwtEmail
            ?: rawMetaEmail
            ?: candidateRawEmail
            ?: "No email provided"

        // Priority order for name: rawName -> jwtClaims["name"] -> metadata["full_name"] -> metadata["name"] -> derived from email
        val computedName = rawName?.ifBlank { null }
            ?: jwtClaims["name"]?.ifBlank { null }
            ?: metadata["full_name"]?.jsonPrimitive?.content?.ifBlank { null }
            ?: metadata["name"]?.jsonPrimitive?.content?.ifBlank { null }
            ?: jwtClaims["given_name"]?.let { g -> 
                val f = jwtClaims["family_name"] ?: ""
                "$g $f".trim()
            }?.ifBlank { null }
            ?: metadata["given_name"]?.jsonPrimitive?.content?.let { g -> 
                val f = metadata["family_name"]?.jsonPrimitive?.content ?: ""
                "$g $f".trim()
            }?.ifBlank { null }
            ?: metadata["preferred_username"]?.jsonPrimitive?.content?.ifBlank { null }
            ?: (if (computedEmail.contains("@")) computedEmail.split("@")[0] else null)
            ?: "Coffee Enthusiast"

        val computedAvatarUrl = rawAvatarUrl?.ifBlank { null }
            ?: jwtClaims["picture"]?.ifBlank { null }
            ?: metadata["avatar_url"]?.jsonPrimitive?.content?.ifBlank { null }
            ?: metadata["picture"]?.jsonPrimitive?.content?.ifBlank { null }

        // Safeguard: Never overwrite a real name or real email with placeholder defaults!
        val finalName = if (
            (computedName == "Coffee Enthusiast" || computedName == "Unknown Name") &&
            existingUser != null &&
            existingUser.name.isNotBlank() &&
            existingUser.name != "Coffee Enthusiast" &&
            existingUser.name != "Unknown Name"
        ) {
            Log.d("AuthRepository", "[PIPELINE_SAFEGUARD] Preserving existing name '${existingUser.name}' instead of placeholder '$computedName'")
            existingUser.name
        } else {
            computedName
        }

        val finalEmail = if (
            (computedEmail == "No email provided" || computedEmail == "No Email Address") &&
            existingUser != null &&
            existingUser.email.isNotBlank() &&
            existingUser.email != "No email provided" &&
            existingUser.email != "No Email Address"
        ) {
            Log.d("AuthRepository", "[PIPELINE_SAFEGUARD] Preserving existing email '${existingUser.email}' instead of placeholder '$computedEmail'")
            existingUser.email
        } else {
            computedEmail
        }

        val finalAvatarUrl = computedAvatarUrl ?: existingUser?.avatarUrl

        val entity = UserEntity(
            id = user.id,
            name = finalName,
            email = finalEmail,
            avatarUrl = finalAvatarUrl
        )

        Log.d("AuthRepository", "[PIPELINE_STEP_5] Final Persisting User Entity: ID=${entity.id}, Name='${entity.name}', Email='${entity.email}'")
        
        try {
            powerSyncDatabase.execute(
                "INSERT OR REPLACE INTO users (id, name, email, avatar_url) VALUES (?, ?, ?, ?)",
                listOf(entity.id, entity.name, entity.email, entity.avatarUrl)
            )
            Log.d("AuthRepository", "[PIPELINE_STEP_6] User profile persisted successfully into local SQLite")
        } catch (e: Exception) {
            Log.e("AuthRepository", "[PIPELINE_ERROR] Failed to persist user profile locally", e)
        }
    }
}

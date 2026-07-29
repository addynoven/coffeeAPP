package com.example.testing1.data.remote

import com.powersync.PowerSyncDatabase
import com.powersync.connectors.PowerSyncBackendConnector
import com.powersync.connectors.PowerSyncCredentials
import io.github.jan.supabase.auth.Auth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PowerSyncConnector @Inject constructor(
    private val auth: Auth
) : PowerSyncBackendConnector() {

    override suspend fun fetchCredentials(): PowerSyncCredentials? {
        // Retrieve session token from Supabase Auth
        val currentSession = auth.currentSessionOrNull() ?: return null

        return PowerSyncCredentials(
            endpoint = "https://YOUR-POWERSYNC-URL.powersync.journeyapps.com", // Your PowerSync Instance Endpoint URL
            token = currentSession.accessToken
        )
    }

    override suspend fun uploadData(database: PowerSyncDatabase) {
        // Handles uploading local offline changes back to Supabase
    }
}
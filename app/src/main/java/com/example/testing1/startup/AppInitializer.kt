package com.example.testing1.startup

import com.example.testing1.data.DatabaseInitializer
import com.example.testing1.data.remote.SupabaseConnector
import com.powersync.PowerSyncDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppInitializer @Inject constructor(
    private val databaseInitializer: DatabaseInitializer,
    private val powerSyncDatabase: PowerSyncDatabase,
    private val supabaseConnector: SupabaseConnector
) {

    suspend fun initialize() {
        databaseInitializer.initialize()

        // Start PowerSync real-time sync engine
        powerSyncDatabase.connect(supabaseConnector)
    }
}
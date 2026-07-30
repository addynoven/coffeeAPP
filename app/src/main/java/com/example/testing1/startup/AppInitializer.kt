package com.example.testing1.startup

import android.util.Log
import com.example.testing1.data.remote.SupabaseConnector
import com.powersync.PowerSyncDatabase
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppInitializer @Inject constructor(
    private val powerSyncDatabase: PowerSyncDatabase,
    private val supabaseConnector: SupabaseConnector
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun initialize() {
        val startTime = System.currentTimeMillis()
        Log.d("AppInitializer", "initialize() started at $startTime")
        
        // Observe auth state to connect/disconnect PowerSync
        scope.launch {
            supabaseConnector.sessionStatus.collectLatest { status ->
                val now = System.currentTimeMillis()
                Log.d("AppInitializer", "Auth Status Changed to $status at $now (+${now - startTime}ms)")
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val connectStart = System.currentTimeMillis()
                        Log.d("AppInitializer", "User authenticated, connecting PowerSync...")
                        try {
                            powerSyncDatabase.connect(supabaseConnector)
                            val connectEnd = System.currentTimeMillis()
                            Log.d("AppInitializer", "powerSyncDatabase.connect() finished in ${connectEnd - connectStart}ms")
                        } catch (e: Exception) {
                            Log.e("AppInitializer", "Failed to connect PowerSync", e)
                        }
                    }
                    is SessionStatus.NotAuthenticated -> {
                        // Using isSignOut logic or session status change
                        Log.d("AppInitializer", "User not authenticated, disconnecting PowerSync...")
                        try {
                            powerSyncDatabase.disconnect()
                        } catch (e: Exception) {
                            Log.e("AppInitializer", "Error during disconnect", e)
                        }
                    }
                    else -> { /* Initializing or others */ }
                }
            }
        }

        // Log PowerSync status changes for debugging
        scope.launch {
            powerSyncDatabase.currentStatus.asFlow().collectLatest { syncStatus ->
                Log.d("AppInitializer", "PowerSync Sync Status: ${syncStatus.connected}")
            }
        }
    }
}

package com.example.testing1.di

import android.content.Context
import com.example.testing1.data.remote.SupabaseConnector
import com.example.testing1.data.schema
import com.powersync.DatabaseDriverFactory
import com.powersync.PowerSyncDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PowerSyncModule {

    @Provides
    @Singleton
    fun provideSupabaseConnector(
        supabaseClient: SupabaseClient
    ): SupabaseConnector {
        return SupabaseConnector(
            supabaseClient = supabaseClient,
            powerSyncEndpoint = "https://6a68b96291ecf2aec48e2bd0.powersync.journeyapps.com"
        )
    }

    @Provides
    @Singleton
    fun providePowerSyncDatabase(
        @ApplicationContext context: Context
    ): PowerSyncDatabase {
        return PowerSyncDatabase(
            factory = DatabaseDriverFactory(context),
            schema = schema
        )
    }
}
package com.example.testing1.di

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://kbhftumajmnqcddcgmil.supabase.co",
            supabaseKey = "sb_publishable_FmfiG2VEFjYbSlAowZsp9Q_H22S8myb"
        ) {
            install(Postgrest)
        }
    }
}

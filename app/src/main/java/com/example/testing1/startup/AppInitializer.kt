package com.example.testing1.startup

import com.example.testing1.data.DatabaseInitializer
import javax.inject.Inject

class AppInitializer @Inject constructor(
    private val databaseInitializer: DatabaseInitializer
) {

    suspend fun initialize() {
        databaseInitializer.initialize()
    }
}
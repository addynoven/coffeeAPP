package com.example.testing1.navigation

import kotlinx.serialization.Serializable

sealed class Routes {

    @Serializable
    data object SplashScreen : Routes()

    @Serializable
    data object WelcomeScreen : Routes()

    @Serializable
    data object LoginScreen : Routes()

    @Serializable
    data object SignUpScreen : Routes()

    @Serializable
    data object HomeScreen : Routes()

    @Serializable
    data class DetailScreen(
        val coffeeId: String
    ) : Routes()

    @Serializable
    data object CartScreen : Routes()

    @Serializable
    data object FavoriteScreen : Routes()

    @Serializable
    data object ProfileScreen : Routes()

    @Serializable
    data object OrderHistoryScreen : Routes()

    @Serializable
    data class TrackOrderScreen(
        val orderId: String
    ) : Routes()

    @Serializable
    data object SettingsScreen : Routes()
}

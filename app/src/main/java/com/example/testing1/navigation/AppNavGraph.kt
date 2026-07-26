package com.example.testing1.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.testing1.screens.cartscreen.CartRoute
import com.example.testing1.screens.detailscreen.DetailRoute
import com.example.testing1.screens.favoritescreen.FavoriteRoute
import com.example.testing1.screens.homescreen.HomeRoute
import com.example.testing1.screens.profilescreen.OrderHistoryRoute
import com.example.testing1.screens.profilescreen.ProfileRoute
import com.example.testing1.screens.settingsscreen.SettingsRoute
import com.example.testing1.screens.welcomescreen.WelcomeScreen


@Composable
fun AppNavGraph() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.WelcomeScreen
    ) {
        composable<Routes.WelcomeScreen> {
            WelcomeScreen(navController)
        }

        composable<Routes.HomeScreen> {
            HomeRoute(
                onItemClick = { item ->
                    navController.navigate(
                        Routes.DetailScreen(item.id)
                    )
                },
                onCartClick = {
                    navController.navigate(Routes.CartScreen)
                },
                onFavoriteClick = {
                    navController.navigate(Routes.FavoriteScreen)
                },
                onProfileClick = {
                    navController.navigate(Routes.ProfileScreen)
                }
            )
        }

        composable<Routes.DetailScreen> { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.DetailScreen>()
            DetailRoute(
                route.coffeeId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable<Routes.FavoriteScreen> {
            FavoriteRoute(
                onHomeClick = {
                    navController.navigate(Routes.HomeScreen)
                },
                onCartClick = {
                    navController.navigate(Routes.CartScreen)
                },
                onFavoriteClick = {
                    navController.navigate(Routes.FavoriteScreen)
                },
                onProfileClick = {
                    navController.navigate(Routes.ProfileScreen)
                }
            )
        }

        composable<Routes.CartScreen> {
            CartRoute(
                onBackClick = {
                    navController.popBackStack()
                },
                onHomeClick = {
                    navController.navigate(Routes.HomeScreen)
                },
                onFavoriteClick = {
                    navController.navigate(Routes.FavoriteScreen)
                },
                onCartClick = {
                    navController.navigate(Routes.CartScreen)
                },
                onProfileClick = {
                    navController.navigate(Routes.ProfileScreen)
                }
            )
        }

        composable<Routes.ProfileScreen> {
            ProfileRoute(
                onHomeClick = {
                    navController.navigate(Routes.HomeScreen)
                },
                onCartClick = {
                    navController.navigate(Routes.CartScreen)
                },
                onFavoriteClick = {
                    navController.navigate(Routes.FavoriteScreen)
                },
                onProfileClick = {
                    navController.navigate(Routes.ProfileScreen)
                },
                onOrdersClick = {
                    navController.navigate(Routes.OrderHistoryScreen)
                },
                onSettingsClick = {
                    navController.navigate(Routes.SettingsScreen)
                }
            )
        }

        composable<Routes.OrderHistoryScreen> {
            OrderHistoryRoute(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable<Routes.SettingsScreen> {
            SettingsRoute(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

package com.example.testing1.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.testing1.MainViewModel
import com.example.testing1.navigation.Routes
import com.example.testing1.screens.auth.LoginRoute
import com.example.testing1.screens.auth.SignUpRoute
import com.example.testing1.screens.cartscreen.CartRoute
import com.example.testing1.screens.detailscreen.DetailRoute
import com.example.testing1.screens.favoritescreen.FavoriteRoute
import com.example.testing1.screens.homescreen.HomeRoute
import com.example.testing1.screens.profilescreen.OrderHistoryRoute
import com.example.testing1.screens.profilescreen.ProfileRoute
import com.example.testing1.screens.settingsscreen.SettingsRoute
import com.example.testing1.screens.trackorderscreen.TrackOrderRoute
import com.example.testing1.screens.welcomescreen.WelcomeScreen


@Composable
fun AppNavGraph(
    viewModel: MainViewModel
) {
    val navController = rememberNavController()
    val startDestination by viewModel.startDestination.collectAsState()

    if (startDestination == null) return

    NavHost(
        navController = navController,
        startDestination = startDestination!!
    ) {
        composable<Routes.WelcomeScreen> {
            WelcomeScreen(navController)
        }

        composable<Routes.LoginScreen> {
            LoginRoute(
                onNavigateToSignUp = {
                    navController.navigate(Routes.SignUpScreen)
                },
                onLoginSuccess = {
                    navController.navigate(Routes.HomeScreen) {
                        popUpTo(Routes.LoginScreen) { inclusive = true }
                    }
                }
            )
        }

        composable<Routes.SignUpScreen> {
            SignUpRoute(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onSignUpSuccess = {
                    // Navigate to Login or show a message
                    navController.popBackStack()
                }
            )
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
                },
                onOrderClick = { orderId ->
                    navController.navigate(Routes.TrackOrderScreen(orderId))
                }
            )
        }

        composable<Routes.TrackOrderScreen> { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.TrackOrderScreen>()
            TrackOrderRoute(
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

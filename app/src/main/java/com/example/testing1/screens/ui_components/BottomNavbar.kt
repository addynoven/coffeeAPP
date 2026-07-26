package com.example.testing1.screens.ui_components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.testing1.R

data class NavItem(
    val title: String,
    val icon: Int
)

@Composable
fun MyBottomBar(selectedTab: String = "Home", onTabSelected: (String) -> Unit = {}) {
    val navItems = listOf(
        NavItem(stringResource(R.string.nav_home), R.drawable.regular_outline_home),
        NavItem(stringResource(R.string.nav_favorite), R.drawable.regular_outline_heart),
        NavItem(stringResource(R.string.nav_cart), R.drawable.regular_outline_bag),
        NavItem(stringResource(R.string.nav_profile), R.drawable.outline_account_circle_24)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.height(70.dp),
        tonalElevation = 8.dp
    ) {
        navItems.forEach { item ->
            NavigationBarItem(
                selected = item.title == selectedTab,
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.title
                    )
                },
                onClick = { onTabSelected(item.title) },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

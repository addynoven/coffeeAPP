package com.example.testing1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.testing1.data.settings.ThemeConfig
import com.example.testing1.navigation.AppNavGraph
import com.example.testing1.ui.theme.Testing1Theme
import com.example.testing1.util.CloudinaryHelper
import com.example.testing1.util.LocalCloudinaryHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var cloudinaryHelper: CloudinaryHelper

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeConfig by viewModel.themeConfig.collectAsState()
            
            val useDarkTheme = when (themeConfig) {
                ThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
                ThemeConfig.LIGHT -> false
                ThemeConfig.DARK -> true
            }

            Testing1Theme(darkTheme = useDarkTheme) {
                CompositionLocalProvider(LocalCloudinaryHelper provides cloudinaryHelper) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavGraph()
                    }
                }
            }
        }
    }
}

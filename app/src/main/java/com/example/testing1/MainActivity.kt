package com.example.testing1

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var cloudinaryHelper: CloudinaryHelper

    @Inject
    lateinit var supabaseClient: SupabaseClient

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle Supabase deep links
        supabaseClient.handleDeeplinks(intent)
        
        enableEdgeToEdge()
        setContent {
            val themeConfig by viewModel.themeConfig.collectAsState()
            val startDestination by viewModel.startDestination.collectAsState()
            
            if (startDestination == null) {
                // Keep showing splash or black screen until we know where to go
                return@setContent
            }
            
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
                        AppNavGraph(viewModel = viewModel)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        supabaseClient.handleDeeplinks(intent)
    }
}

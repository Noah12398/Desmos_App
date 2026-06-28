package com.noah.desmos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.noah.desmos.auth.data.AuthRepository
import com.noah.desmos.auth.ui.AuthViewModel
import com.noah.desmos.auth.ui.AuthViewModelFactory
import com.noah.desmos.local.datastore.TokenManager
import com.noah.desmos.navigation.AppNavGraph
import com.noah.desmos.network.ApiClient

import android.content.Intent
import com.noah.desmos.auth.data.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import android.util.Log
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle deep link on startup
        Log.d("AuthFlow", "onCreate – handling intent: ${intent?.data}")
        SupabaseClient.client.handleDeeplinks(intent)

        enableEdgeToEdge()

        setContent {

            val navController =
                rememberNavController()

            val api =
                ApiClient.authApi(this)

            val repository =
                AuthRepository(
                    api,
                    TokenManager(this)
                )

            val authViewModel: AuthViewModel =
                viewModel(
                    factory =
                        AuthViewModelFactory(
                            repository
                        )
                )

            AppNavGraph(
                navController,
                authViewModel
            )

        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("AuthFlow", "onNewIntent – handling intent: ${intent?.data}")
        SupabaseClient.client.handleDeeplinks(intent)
    }
}
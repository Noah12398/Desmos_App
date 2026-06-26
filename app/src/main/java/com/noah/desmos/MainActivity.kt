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

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

}
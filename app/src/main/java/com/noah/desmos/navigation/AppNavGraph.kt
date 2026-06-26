package com.noah.desmos.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.noah.desmos.auth.ui.AuthViewModel
import com.noah.desmos.auth.ui.OtpScreen
import com.noah.desmos.auth.ui.SignupScreen
import com.noah.desmos.auth.ui.CompleteProfileScreen
import com.noah.desmos.family.ui.HomeScreen
import com.noah.desmos.navigation.Screen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {

    NavHost(
        navController = navController,
        startDestination = Screen.Signup.route
    ) {

        composable(Screen.Otp.route) {

            OtpScreen(
                navController = navController,
                viewModel = authViewModel
            )

        }

        composable(Screen.Signup.route) {

            SignupScreen(
                navController = navController,
                viewModel = authViewModel
            )

        }

        composable(Screen.CompleteProfile.route) {
            CompleteProfileScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }

            composable(Screen.Home.route) {
                HomeScreen()
            }

        }

    }


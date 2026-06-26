package com.noah.desmos.navigation

sealed class Screen(val route: String) {

    data object Login : Screen("login")

    data object Otp : Screen("otp")

    data object Signup : Screen("signup")

    data object CompleteProfile : Screen("completeProfile")

    data object Home : Screen("home")

}
package com.noah.desmos.auth.data

data class SignInRequest(
    val fcm_token: String? = null
)

data class RegisterRequest(
    val name: String,
    val fcm_token: String? = null
)
package com.noah.desmos.auth.data

import com.noah.desmos.network.ApiResponse

data class UserResponse(
    val id: String,
    val name: String,
    val phone: String,
    val createdAt: String
)

/**
 * Shape of the data field returned by POST /auth/signin.
 * Backend returns { isNewUser: Boolean, user?: UserResponse }.
 */
data class SignInData(
    val isNewUser: Boolean,
    val user: UserResponse? = null
)

sealed class SignInResult {
    data class Success(val user: UserResponse) : SignInResult()
    object NewUser : SignInResult()
}
package com.noah.desmos.auth.data

data class UserResponse(
    val id: String,
    val name: String,
    val phone: String,
    val createdAt: String
)

/**
 * Generic API response wrapper used by the backend.
 * All endpoints return { success, data?, message? }.
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
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
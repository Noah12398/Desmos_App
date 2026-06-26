package com.noah.desmos.auth.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/signin")
    suspend fun signIn(
        @Body request: SignInRequest
    ): Response<ApiResponse<SignInData>>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<UserResponse>>

    @GET("auth/me")
    suspend fun me(): Response<ApiResponse<UserResponse>>
}
package com.noah.desmos.auth.data

import com.noah.desmos.local.datastore.TokenManager
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val api: AuthApi,
    private val tokenManager: TokenManager
) {

    suspend fun sendOtp(phone: String): Result<Unit> {

        return withContext(Dispatchers.IO) {

            try {

                SupabaseClient.client.auth.signInWith(OTP) {
                    this.phone = phone
                }

                Result.success(Unit)

            } catch (e: Exception) {

                Result.failure(e)

            }

        }

    }

    /**
     * Verify OTP with Supabase, then call POST /auth/signin.
     * Backend returns { isNewUser: bool, user?: {...} }.
     */
    suspend fun verifyOtp(
        phone: String,
        otp: String,
        fcmToken: String? = null
    ): Result<SignInResult> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.auth.verifyPhoneOtp(
                    type = OtpType.Phone.SMS,
                    phone = phone,
                    token = otp
                )

                val session = SupabaseClient.client.auth.currentSessionOrNull()
                val accessToken = session?.accessToken ?: throw Exception("No token")

                tokenManager.saveToken(accessToken)

                // Call backend signin
                val response = api.signIn(
                    SignInRequest(fcm_token = fcmToken)
                )

                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Signin failed"))
                }

                val body = response.body()
                if (body?.success == true && body.data != null) {
                    if (body.data.isNewUser) {
                        // New user — app should navigate to CompleteProfileScreen
                        Result.success(SignInResult.NewUser)
                    } else if (body.data.user != null) {
                        // Existing user — go straight to Home
                        Result.success(SignInResult.Success(body.data.user))
                    } else {
                        Result.failure(Exception("Unexpected response"))
                    }
                } else {
                    Result.failure(Exception(body?.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Register a new user after CompleteProfileScreen.
     * Calls POST /auth/register with { name, fcm_token? }.
     */
    suspend fun register(request: RegisterRequest): Result<SignInResult> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.register(request)
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Registration failed"))
                }
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(SignInResult.Success(body.data))
                } else {
                    Result.failure(Exception(body?.message ?: "Unexpected error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getProfile(): Result<UserResponse> {

        return withContext(Dispatchers.IO) {

            try {

                val response = api.me()

                if (response.isSuccessful &&
                    response.body()?.data != null
                ) {

                    Result.success(
                        response.body()!!.data!!
                    )

                } else {

                    Result.failure(
                        Exception("Unable to fetch profile")
                    )

                }

            } catch (e: Exception) {

                Result.failure(e)

            }

        }

    }

}
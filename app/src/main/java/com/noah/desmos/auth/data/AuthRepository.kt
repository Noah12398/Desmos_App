package com.noah.desmos.auth.data

import com.noah.desmos.local.datastore.TokenManager
import com.noah.desmos.network.safeApiCall
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
        return try {
            withContext(Dispatchers.IO) {
                SupabaseClient.client.auth.verifyPhoneOtp(
                    type = OtpType.Phone.SMS,
                    phone = phone,
                    token = otp
                )

                val session = SupabaseClient.client.auth.currentSessionOrNull()
                val accessToken = session?.accessToken ?: throw Exception("No access token")
                val refreshToken = session?.refreshToken ?: throw Exception("No refresh token")

                tokenManager.saveTokens(accessToken, refreshToken)
            }

            safeApiCall { api.signIn(SignInRequest(fcm_token = fcmToken)) }
                .mapCatching { signInData ->
                    when {
                        signInData.isNewUser -> SignInResult.NewUser
                        signInData.user != null -> SignInResult.Success(signInData.user)
                        else -> throw Exception("Unexpected signin response shape")
                    }
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Register a new user after CompleteProfileScreen.
     * Calls POST /auth/register with { name, fcm_token? }.
     */
    suspend fun register(request: RegisterRequest): Result<SignInResult> {
        return safeApiCall { api.register(request) }
            .map { userResponse -> SignInResult.Success(userResponse) }
    }

    suspend fun getProfile(): Result<UserResponse> {
        return safeApiCall { api.me() }
    }

}
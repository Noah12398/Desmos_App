package com.noah.desmos.auth.data

import com.noah.desmos.local.datastore.TokenManager
import com.noah.desmos.network.safeApiCall
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
class AuthRepository(
    private val api: AuthApi,
    private val tokenManager: TokenManager
) {

    suspend fun signInWithGoogle(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.auth.signInWith(
                    provider = Google,
                    redirectUrl = "desmos://login"
                )
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun saveSessionTokens(accessToken: String, refreshToken: String) {
        withContext(Dispatchers.IO) {
            tokenManager.saveTokens(accessToken, refreshToken)
        }
    }

    suspend fun signIn(fcmToken: String? = null): Result<SignInResult> {
        return safeApiCall { api.signIn(SignInRequest(fcm_token = fcmToken)) }
            .mapCatching { signInData ->
                when {
                    signInData.isNewUser -> SignInResult.NewUser
                    signInData.user != null -> SignInResult.Success(signInData.user)
                    else -> throw Exception("Unexpected signin response shape")
                }
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
package com.noah.desmos.network

import com.noah.desmos.auth.data.SupabaseClient
import com.noah.desmos.local.datastore.TokenManager
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * An OkHttp Authenticator that intercepts 401 Unauthorized API responses
 * and automatically attempts to refresh the expired Supabase access token
 * using the stored refresh token.
 */
class TokenAuthenticator(
    private val tokenManager: TokenManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite loops if refreshing fails repeatedly
        if (responseCount(response) >= 3) {
            return null
        }

        val refreshToken = runBlocking {
            tokenManager.getRefreshToken()
        } ?: return null

        return try {
            // Trigger Supabase refresh session
            val newSession = runBlocking {
                SupabaseClient.client.auth.refreshSession(refreshToken)
            }

            // Save the newly acquired tokens
            runBlocking {
                tokenManager.saveTokens(newSession.accessToken, newSession.refreshToken)
            }

            // Retry request with the fresh token
            response.request.newBuilder()
                .header("Authorization", "Bearer ${newSession.accessToken}")
                .build()
        } catch (e: Exception) {
            // If the refresh token itself is invalid/expired, clear storage to trigger log out
            runBlocking {
                tokenManager.clearTokens()
            }
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            result++
            priorResponse = priorResponse.priorResponse
        }
        return result
    }
}

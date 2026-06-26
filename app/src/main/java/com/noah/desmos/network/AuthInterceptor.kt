package com.noah.desmos.network

import com.noah.desmos.local.datastore.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val token = runBlocking {
            tokenManager.getToken()
        }

        val request = chain.request()
            .newBuilder()
            .apply {

                if (!token.isNullOrEmpty()) {

                    addHeader(
                        "Authorization",
                        "Bearer $token"
                    )

                }

            }
            .build()

        return chain.proceed(request)

    }
}
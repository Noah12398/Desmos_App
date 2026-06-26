package com.noah.desmos.network

import android.content.Context
import com.noah.desmos.auth.data.AuthApi
import com.noah.desmos.local.datastore.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL =
        "http://10.0.2.2:3000/"

    fun authApi(
        context: Context
    ): AuthApi {

        val tokenManager = TokenManager(context)

        val logging = HttpLoggingInterceptor()

        logging.level =
            HttpLoggingInterceptor.Level.BODY

        val client =
            OkHttpClient.Builder()
                .authenticator(TokenAuthenticator(tokenManager))
                .addInterceptor(
                    AuthInterceptor(tokenManager)
                )
                .addInterceptor(logging)
                .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(AuthApi::class.java)

    }

}
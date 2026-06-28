package com.noah.desmos.auth.data

import com.noah.desmos.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth

object SupabaseClient {

    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {

        install(Auth) {
            scheme = "desmos"
            host = "login"
        }

    }
}
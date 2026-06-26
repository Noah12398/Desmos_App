package com.noah.desmos.auth.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth

object SupabaseClient {

    val client = createSupabaseClient(
        supabaseUrl = "https://YOUR_PROJECT.supabase.co",
        supabaseKey = "YOUR_ANON_KEY"
    ) {

        install(Auth)

    }
}
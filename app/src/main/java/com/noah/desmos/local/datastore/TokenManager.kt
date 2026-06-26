package com.noah.desmos.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore("auth")

class TokenManager(
    private val context: Context
) {

    companion object {

        private val TOKEN =
            stringPreferencesKey("token")

    }

    suspend fun saveToken(
        token: String
    ) {

        context.dataStore.edit {

            it[TOKEN] = token

        }

    }

    suspend fun getToken(): String? {

        val pref =
            context.dataStore.data.first()

        return pref[TOKEN]

    }

    suspend fun clearToken() {

        context.dataStore.edit {

            it.remove(TOKEN)

        }

    }

}
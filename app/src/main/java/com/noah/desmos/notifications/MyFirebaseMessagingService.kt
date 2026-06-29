package com.noah.desmos.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.noah.desmos.auth.data.AuthRepository
import com.noah.desmos.local.datastore.TokenManager
import com.noah.desmos.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")
        
        val api = ApiClient.authApi(applicationContext)
        val tokenManager = TokenManager(applicationContext)
        val repository = AuthRepository(api, tokenManager)
        
        scope.launch {
            // Check if we have an active access token before attempting to sync with the backend
            val currentToken = tokenManager.getToken()
            if (!currentToken.isNullOrEmpty()) {
                val result = repository.updateFcmToken(token)
                result.onSuccess {
                    Log.d("FCM", "Successfully sent new FCM token to backend")
                }
                result.onFailure { error ->
                    Log.e("FCM", "Failed to update FCM token on backend", error)
                }
            } else {
                Log.d("FCM", "No active user session, skipped sending token to backend")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}

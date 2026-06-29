package com.noah.desmos.auth.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noah.desmos.auth.data.AuthRepository
import com.noah.desmos.auth.data.RegisterRequest
import com.noah.desmos.auth.data.SignInResult
import com.noah.desmos.auth.data.SupabaseClient
import com.noah.desmos.auth.model.User
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.coroutines.resume

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    var loading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var loggedInUser by mutableStateOf<User?>(null)
        private set

    var isNewUser by mutableStateOf(false)
        private set

    var name by mutableStateOf("")
        private set

    var fcmToken by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            SupabaseClient.client.auth.sessionStatus.collectLatest { status ->
                if (status is SessionStatus.Authenticated) {
                    val session = status.session
                    repository.saveSessionTokens(session.accessToken, session.refreshToken)
                    
                    loading = true
                    errorMessage = null

                    // Fetch FCM token before calling /signin so it's included in the request
                    val token = fetchFcmToken()
                    fcmToken = token
                    
                    val result = repository.signIn(fcmToken)
                    
                    loading = false
                    
                    result.onSuccess { signInResult ->
                        when (signInResult) {
                            is SignInResult.Success -> {
                                loggedInUser = User(
                                    id = signInResult.user.id,
                                    name = signInResult.user.name,
                                    email = signInResult.user.email,
                                    createdAt = signInResult.user.createdAt
                                )
                                isNewUser = false
                            }
                            is SignInResult.NewUser -> {
                                isNewUser = true
                            }
                        }
                    }
                    
                    result.onFailure {
                        errorMessage = it.message
                    }
                }
            }
        }
    }

    /**
     * Suspends and returns the FCM registration token, or null on failure.
     */
    private suspend fun fetchFcmToken(): String? {
        return suspendCancellableCoroutine { cont ->
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    Log.d("AuthFlow", "FCM token obtained: ${token.take(10)}…")
                    cont.resume(token)
                }
                .addOnFailureListener { e ->
                    Log.e("AuthFlow", "Failed to get FCM token", e)
                    cont.resume(null)
                }
        }
    }

    fun onNameChanged(value: String) {
        name = value
    }

    fun updateFcmToken(token: String?) {
        fcmToken = token
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            loading = true
            errorMessage = null
            val result = repository.signInWithGoogle()
            loading = false
            result.onFailure {
                errorMessage = it.message
            }
        }
    }

    fun register() {
        if (name.isBlank()) {
            errorMessage = "Enter name"
            return
        }
        viewModelScope.launch {
            loading = true
            errorMessage = null

            // Ensure FCM token is fresh for registration too
            if (fcmToken == null) {
                fcmToken = fetchFcmToken()
            }

            val request = RegisterRequest(
                name = name,
                fcm_token = fcmToken
            )
            val result = repository.register(request)
            loading = false
            result.onSuccess { signInResult ->
                when (signInResult) {
                    is SignInResult.Success -> {
                        loggedInUser = User(
                            id = signInResult.user.id,
                            name = signInResult.user.name,
                            email = signInResult.user.email,
                            createdAt = signInResult.user.createdAt
                        )
                        isNewUser = false
                    }
                    is SignInResult.NewUser -> {
                        isNewUser = true
                    }
                }
            }
            result.onFailure {
                errorMessage = it.message
                isNewUser = false
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            loading = true
            val result = repository.getProfile()
            loading = false
            result.onSuccess {
                loggedInUser = User(
                    id = it.id,
                    name = it.name,
                    email = it.email,
                    createdAt = it.createdAt
                )
            }
            result.onFailure {
                errorMessage = it.message
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }

    fun logout() {
        viewModelScope.launch {
            SupabaseClient.client.auth.signOut()
            loggedInUser = null
            isNewUser = false
            name = ""
        }
    }
}

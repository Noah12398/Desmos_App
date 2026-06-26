package com.noah.desmos.auth.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noah.desmos.auth.data.AuthRepository
import com.noah.desmos.auth.data.RegisterRequest
import com.noah.desmos.auth.data.SignInResult
import com.noah.desmos.auth.model.User
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    var phone by mutableStateOf("")
        private set

    var otp by mutableStateOf("")
        private set

    var loading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var otpSent by mutableStateOf(false)
        private set

    var loggedInUser by mutableStateOf<User?>(null)
        private set

    var isNewUser by mutableStateOf(false)
        private set
    var name by mutableStateOf("")
        private set

    var fcmToken by mutableStateOf<String?>(null)
        private set

    fun onNameChanged(value: String) {
        name = value
    }

    fun setFcmToken(token: String?) {
        fcmToken = token
    }

    fun onPhoneChanged(value: String) {
        phone = value
    }

    fun onOtpChanged(value: String) {
        otp = value
    }

    fun sendOtp() {

        if (phone.isBlank()) {
            errorMessage = "Enter phone number"
            return
        }

        viewModelScope.launch {

            loading = true
            errorMessage = null

            val result = repository.sendOtp(phone)

            loading = false

            result.onSuccess {
                otpSent = true
            }

            result.onFailure {
                errorMessage = it.message
            }

        }
    }

    fun verifyOtp() {

        if (otp.isBlank()) {
            errorMessage = "Enter OTP"
            return
        }

        viewModelScope.launch {

            loading = true
            errorMessage = null

            val result = repository.verifyOtp(
                phone = phone,
                otp = otp,
                fcmToken = fcmToken
            )

            loading = false

            result.onSuccess { signInResult ->
                when (signInResult) {
                    is SignInResult.Success -> {
                        loggedInUser = User(
                            id = signInResult.user.id,
                            name = signInResult.user.name,
                            phone = signInResult.user.phone,
                            createdAt = signInResult.user.createdAt
                        )
                        isNewUser = false
                    }
                    is SignInResult.NewUser -> {
                        // User exists but needs to register profile (provide name)
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

    fun register() {
        if (name.isBlank()) {
            errorMessage = "Enter name"
            return
        }
        viewModelScope.launch {
            loading = true
            errorMessage = null
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
                            phone = signInResult.user.phone,
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
                    phone = it.phone,
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
        loggedInUser = null
        otpSent = false
        phone = ""
        otp = ""
    }
}
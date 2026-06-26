package com.noah.desmos.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response

/**
 * Common API response envelope used across all network calls in the app.
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)

/**
 * A global helper function to execute Retrofit API calls safely.
 * - Switches execution context to Dispatchers.IO.
 * - Standardizes success checking and result mapping.
 * - Extracts clean user-facing error messages from server JSON error bodies.
 * - Catches raw connection/parsing exceptions safely.
 */
suspend fun <T> safeApiCall(
    call: suspend () -> Response<ApiResponse<T>>
): Result<T> = withContext(Dispatchers.IO) {
    try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body?.success == true && body.data != null) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Response flag returned failure or data was null"))
            }
        } else {
            val errorMsg = try {
                val errorBodyString = response.errorBody()?.string()
                if (!errorBodyString.isNullOrEmpty()) {
                    val json = JSONObject(errorBodyString)
                    json.optString("message", "Request failed (HTTP ${response.code()})")
                } else {
                    "Request failed (HTTP ${response.code()})"
                }
            } catch (e: Exception) {
                "Request failed (HTTP ${response.code()})"
            }
            Result.failure(Exception(errorMsg))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

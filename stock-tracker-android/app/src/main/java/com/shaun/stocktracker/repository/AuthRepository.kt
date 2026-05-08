package com.shaun.stocktracker.repository

import android.util.Log
import com.shaun.stocktracker.network.RetrofitClient
import com.shaun.stocktracker.util.EventLogger
import com.shaun.stocktracker.util.NetworkResult
import com.shaun.stocktracker.util.SessionManager
import kotlinx.coroutines.delay

/**
 * Repository for authentication and session management.
 * Handles login, session status checks, and retry with backoff.
 *
 * Primary auth handling: called by PricePollingService on 401/403 detection.
 * Fallback auth handling: SessionRefreshWorker calls refreshSessionIfNeeded().
 */
class AuthRepository(private val sessionManager: SessionManager) {

    companion object {
        private const val TAG = "AuthRepository"
        private const val MAX_RETRIES = 3
        private const val BASE_RETRY_DELAY_MS = 3000L
    }

    /**
     * Check if the current backend session is active.
     * @return NetworkResult.Success(true/false) or .Error
     */
    suspend fun isSessionActive(): NetworkResult<Boolean> {
        return try {
            val api = RetrofitClient.getApiService(sessionManager)
            val response = api.getSessionStatus()
            if (response.isSuccessful) {
                val active = response.body()?.loggedIn == true
                sessionManager.setSessionActive(active)
                NetworkResult.Success(active)
            } else {
                Log.w(TAG, "Status check failed: HTTP ${response.code()}")
                sessionManager.setSessionActive(false)
                NetworkResult.Error("Status check failed: HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Status check error", e)
            sessionManager.setSessionActive(false)
            NetworkResult.Error(e.message ?: "Status check error")
        }
    }

    /**
     * Attempt to log in to the backend (which triggers Angel One auth server-side).
     * Includes retry with exponential backoff.
     *
     * @return NetworkResult.Success(message) or .Error(message)
     */
    suspend fun login(): NetworkResult<String> {
        var lastMessage = "Login failed"

        repeat(MAX_RETRIES) { attempt ->
            try {
                val api = RetrofitClient.getApiService(sessionManager)
                val response = api.login()

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        sessionManager.setSessionActive(true)
                        sessionManager.updateLastLoginTime()
                        EventLogger.log(EventLogger.Event.LOGIN_SUCCESS,
                            "Login successful on attempt ${attempt + 1}")
                        return NetworkResult.Success(body.message)
                    } else {
                        lastMessage = body?.message ?: "Login failed"
                        Log.w(TAG, "Login returned failure: $lastMessage")
                    }
                } else {
                    Log.w(TAG, "Login HTTP ${response.code()} on attempt ${attempt + 1}")
                    lastMessage = "HTTP ${response.code()}: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login error on attempt ${attempt + 1}", e)
                lastMessage = e.message ?: "Login error"
            }

            if (attempt < MAX_RETRIES - 1) {
                val delayMs = BASE_RETRY_DELAY_MS * (attempt + 1)
                Log.d(TAG, "Retrying login in ${delayMs}ms...")
                delay(delayMs)
            }
        }

        sessionManager.setSessionActive(false)
        EventLogger.log(EventLogger.Event.LOGIN_FAILURE,
            "Login failed after $MAX_RETRIES attempts: $lastMessage")
        return NetworkResult.Error(lastMessage)
    }

    /**
     * Called by PricePollingService when it detects 401/403.
     * Attempts a direct re-login without checking status first (we already know it's expired).
     *
     * @return true if re-login succeeded
     */
    suspend fun refreshSession(): Boolean {
        EventLogger.log(EventLogger.Event.SESSION_REFRESH, "Direct session refresh triggered by service")
        val result = login()
        return result.isSuccess
    }

    /**
     * Fallback: called by SessionRefreshWorker as a safety net.
     * Checks status first, then re-logins only if needed.
     *
     * @return true if session is active (either already was, or refreshed)
     */
    suspend fun refreshSessionIfNeeded(): Boolean {
        val statusResult = isSessionActive()
        val isActive = statusResult.getOrDefault(false)

        if (isActive) {
            Log.d(TAG, "Session still active, no refresh needed")
            return true
        }

        Log.i(TAG, "Session expired, attempting re-login...")
        EventLogger.log(EventLogger.Event.SESSION_REFRESH, "Fallback session refresh by worker")
        val result = login()
        return result.isSuccess
    }
}

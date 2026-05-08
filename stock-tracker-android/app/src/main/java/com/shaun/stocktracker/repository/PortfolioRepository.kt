package com.shaun.stocktracker.repository

import android.util.Log
import com.shaun.stocktracker.network.PriceDataSource
import com.shaun.stocktracker.network.RetrofitClient
import com.shaun.stocktracker.network.model.Holding
import com.shaun.stocktracker.util.EventLogger
import com.shaun.stocktracker.util.NetworkResult
import com.shaun.stocktracker.util.SessionManager
import kotlinx.coroutines.delay

/**
 * Repository for portfolio data. Implements PriceDataSource so the
 * polling data source can later be swapped for WebSocket without
 * changing AlertEvaluator, services, or other repositories.
 *
 * All network calls for portfolio go through here.
 * Services and workers must never call Retrofit directly.
 */
class PortfolioRepository(
    private val sessionManager: SessionManager
) : PriceDataSource {

    companion object {
        private const val TAG = "PortfolioRepository"
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 2000L
    }

    /**
     * Fetches the current portfolio holdings from the backend.
     * Includes retry logic with exponential backoff.
     *
     * @return NetworkResult.Success, .Error, or .Unauthorized
     */
    override suspend fun fetchHoldings(): NetworkResult<List<Holding>> {
        var lastMessage = "Unknown error"

        repeat(MAX_RETRIES) { attempt ->
            try {
                val api = RetrofitClient.getApiService(sessionManager)
                val response = api.getPortfolio()

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        return NetworkResult.Success(body.holdings)
                    } else {
                        val msg = body?.message ?: "Unknown error"
                        Log.w(TAG, "API returned failure: $msg")
                        return NetworkResult.Error(msg)
                    }
                } else if (response.code() == 401 || response.code() == 403) {
                    EventLogger.log(EventLogger.Event.SESSION_EXPIRED, "HTTP ${response.code()} from /api/portfolio")
                    return NetworkResult.Unauthorized("Session expired (HTTP ${response.code()})")
                } else {
                    Log.w(TAG, "HTTP ${response.code()} on attempt ${attempt + 1}")
                    lastMessage = "HTTP ${response.code()}: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error on attempt ${attempt + 1}", e)
                lastMessage = e.message ?: "Network error"
                EventLogger.log(EventLogger.Event.API_ERROR, "Portfolio fetch error: $lastMessage",
                    mapOf("attempt" to "${attempt + 1}"))
            }

            if (attempt < MAX_RETRIES - 1) {
                delay(RETRY_DELAY_MS * (attempt + 1))
            }
        }

        EventLogger.log(EventLogger.Event.POLL_FAILURE, "Failed after $MAX_RETRIES retries: $lastMessage")
        return NetworkResult.Error("Failed after $MAX_RETRIES retries: $lastMessage")
    }

    /**
     * Convenience alias matching the old API name.
     * Delegates to fetchHoldings() for backward compatibility.
     */
    suspend fun getHoldings(): NetworkResult<List<Holding>> = fetchHoldings()
}

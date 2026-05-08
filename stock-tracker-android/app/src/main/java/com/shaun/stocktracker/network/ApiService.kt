package com.shaun.stocktracker.network

import com.shaun.stocktracker.network.model.LoginResponse
import com.shaun.stocktracker.network.model.PortfolioResponse
import com.shaun.stocktracker.network.model.StatusResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Retrofit interface for the FastAPI backend.
 * All endpoints match the backend's main.py routes.
 */
interface ApiService {

    @POST("/api/login")
    suspend fun login(): Response<LoginResponse>

    @GET("/api/status")
    suspend fun getSessionStatus(): Response<StatusResponse>

    @GET("/api/portfolio")
    suspend fun getPortfolio(): Response<PortfolioResponse>
}

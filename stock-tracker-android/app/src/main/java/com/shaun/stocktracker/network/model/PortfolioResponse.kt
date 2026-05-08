package com.shaun.stocktracker.network.model

import com.google.gson.annotations.SerializedName

/**
 * Response wrapper from GET /api/portfolio.
 * Matches the actual FastAPI backend response format.
 */
data class PortfolioResponse(
    val success: Boolean,
    val message: String,
    val holdings: List<Holding>
)

/**
 * Single stock holding from the backend.
 * Field names mapped from the Angel One SmartAPI response
 * (tradingsymbol, averageprice, etc.) to clean Kotlin property names.
 */
data class Holding(
    @SerializedName("tradingsymbol")
    val symbol: String,

    @SerializedName("exchange")
    val exchange: String,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("averageprice")
    val avgPrice: Double,

    @SerializedName("ltp")
    val ltp: Double,

    @SerializedName("profitandloss")
    val pnl: Double,

    @SerializedName("pnlpercentage")
    val pnlPercentage: Double
)

/**
 * Response from POST /api/login.
 */
data class LoginResponse(
    val success: Boolean,
    val message: String
)

/**
 * Response from GET /api/status.
 */
data class StatusResponse(
    @SerializedName("logged_in")
    val loggedIn: Boolean,
    val message: String
)

package com.shaun.stocktracker.network

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface YahooFinanceService {

    @GET("v8/finance/chart/{symbol}")
    suspend fun getChartData(
        @Path("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("range") range: String
    ): YahooChartResponse

    companion object {
        private const val BASE_URL = "https://query1.finance.yahoo.com/"

        fun create(): YahooFinanceService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(YahooFinanceService::class.java)
        }
    }
}

data class YahooChartResponse(
    val chart: Chart?
)

data class Chart(
    val result: List<ChartResult>?,
    val error: Any?
)

data class ChartResult(
    val meta: ChartMeta?,
    val timestamp: List<Long>?,
    val indicators: Indicators?
)

data class ChartMeta(
    val currency: String?,
    val symbol: String?,
    val exchangeName: String?,
    val instrumentType: String?,
    val firstTradeDate: Long?,
    val regularMarketTime: Long?,
    val gmtoffset: Int?,
    val timezone: String?,
    val exchangeTimezoneName: String?,
    val regularMarketPrice: Double?,
    val chartPreviousClose: Double?,
    val previousClose: Double?
)

data class Indicators(
    val quote: List<Quote>?
)

data class Quote(
    val open: List<Double?>?,
    val low: List<Double?>?,
    val high: List<Double?>?,
    val close: List<Double?>?,
    val volume: List<Long?>?
)

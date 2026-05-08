package com.shaun.stocktracker.network

import com.shaun.stocktracker.network.model.Holding
import com.shaun.stocktracker.util.NetworkResult

/**
 * Abstraction for fetching live price data.
 *
 * Current implementation: PollingPriceDataSource (HTTP polling via Retrofit)
 * Future implementation: WebSocketPriceDataSource (live streaming)
 *
 * Services and evaluators depend on this interface, NOT on Retrofit directly.
 * Swapping from polling to WebSocket requires only a new implementation —
 * no changes to AlertEvaluator, repositories, or service orchestration.
 */
interface PriceDataSource {

    /**
     * Fetch the latest holdings with live prices.
     * @return NetworkResult wrapping the holdings list
     */
    suspend fun fetchHoldings(): NetworkResult<List<Holding>>
}

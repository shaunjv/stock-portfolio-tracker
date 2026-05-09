package com.shaun.stocktracker.ui.navigation

/**
 * Route definitions for Compose Navigation.
 * Each screen defines its route string used by NavHost.
 */
sealed class Screen(val route: String) {

    /** Portfolio dashboard — list of all holdings. */
    object Dashboard : Screen("dashboard")

    /** Stock detail — shows full info for a single holding. */
    object StockDetail : Screen("stock_detail/{symbol}") {
        /** Build the actual route with a concrete symbol value. */
        fun createRoute(symbol: String): String = "stock_detail/$symbol"

        /** Nav argument key. */
        const val ARG_SYMBOL = "symbol"
    }
}

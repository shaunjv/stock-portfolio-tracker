package com.shaun.stocktracker.ui.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.shaun.stocktracker.ui.dashboard.DashboardScreen
import com.shaun.stocktracker.ui.dashboard.DashboardViewModel
import com.shaun.stocktracker.ui.detail.StockDetailScreen
import com.shaun.stocktracker.ui.detail.StockDetailViewModel

/**
 * Top-level Compose NavHost for the Portfolio tab.
 * Dashboard → StockDetail navigation.
 *
 * DashboardViewModel is Activity-scoped (passed in),
 * StockDetailViewModel is per-destination (created here).
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    dashboardViewModel: DashboardViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        // ── Dashboard ────────────────────────────────────────
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToDetail = { symbol ->
                    navController.navigate(Screen.StockDetail.createRoute(symbol))
                },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }

        // ── Stock Detail ─────────────────────────────────────
        composable(
            route = Screen.StockDetail.route,
            arguments = listOf(
                navArgument(Screen.StockDetail.ARG_SYMBOL) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val symbol = backStackEntry.arguments
                ?.getString(Screen.StockDetail.ARG_SYMBOL) ?: ""

            // Holding data from the Activity-scoped DashboardViewModel cache
            val holding = dashboardViewModel.getHoldingBySymbol(symbol)

            // StockDetailViewModel scoped to this nav entry (alert operations)
            val application = LocalContext.current.applicationContext as Application
            val detailViewModel: StockDetailViewModel = viewModel(
                factory = StockDetailViewModel.Factory(application, symbol)
            )

            StockDetailScreen(
                holding = holding,
                viewModel = detailViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFullChart = { sym ->
                    navController.navigate(Screen.FullChart.createRoute(sym))
                }
            )
        }

        // ── Full Chart ───────────────────────────────────────
        composable(
            route = Screen.FullChart.route,
            arguments = listOf(
                navArgument(Screen.FullChart.ARG_SYMBOL) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val symbol = backStackEntry.arguments?.getString(Screen.FullChart.ARG_SYMBOL) ?: ""
            val holding = dashboardViewModel.getHoldingBySymbol(symbol)
            com.shaun.stocktracker.ui.detail.FullChartScreen(
                symbol = symbol,
                currentPrice = holding?.ltp,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

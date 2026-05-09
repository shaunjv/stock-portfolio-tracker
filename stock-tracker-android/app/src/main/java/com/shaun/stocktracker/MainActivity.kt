package com.shaun.stocktracker

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.shaun.stocktracker.repository.AuthRepository
import com.shaun.stocktracker.repository.PortfolioRepository
import com.shaun.stocktracker.ui.alerts.AlertsFragment
import com.shaun.stocktracker.ui.dashboard.DashboardViewModel
import com.shaun.stocktracker.ui.navigation.AppNavGraph
import com.shaun.stocktracker.util.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var dashboardViewModel: DashboardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize shared dependencies
        val sessionManager = SessionManager(applicationContext)
        val portfolioRepository = PortfolioRepository(sessionManager)
        val authRepository = AuthRepository(sessionManager)

        // 2. Create Activity-scoped DashboardViewModel
        val factory = DashboardViewModel.Factory(portfolioRepository, authRepository)
        dashboardViewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]

        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }
            val navController = rememberNavController()
            var selectedTab by remember { mutableStateOf(0) } // 0 = Portfolio, 1 = Alerts

            MaterialTheme(
                colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
            ) {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Default.List, contentDescription = "Portfolio") },
                                label = { Text("Portfolio") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Default.Notifications, contentDescription = "Alerts") },
                                label = { Text("Alerts") }
                            )
                        }
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        when (selectedTab) {
                            0 -> {
                                // Tab 1: Compose Navigation (Dashboard -> Detail)
                                AppNavGraph(
                                    navController = navController,
                                    dashboardViewModel = dashboardViewModel,
                                    isDarkTheme = isDarkTheme,
                                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                                )
                            }
                            1 -> {
                                // Tab 2: Existing XML Fragment
                                AlertsFragmentContainer()
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compose wrapper to host the legacy XML AlertsFragment.
 */
@Composable
fun AlertsFragmentContainer() {
    AndroidView(
        factory = { context ->
            FragmentContainerView(context).apply {
                id = View.generateViewId()
                val fragmentManager = (context as AppCompatActivity).supportFragmentManager
                fragmentManager.commit {
                    replace(id, AlertsFragment())
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

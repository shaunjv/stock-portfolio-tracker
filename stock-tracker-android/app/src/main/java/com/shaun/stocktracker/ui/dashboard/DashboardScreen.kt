package com.shaun.stocktracker.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.filled.MoveToInbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shaun.stocktracker.network.model.Holding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToDetail: (String) -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Portfolio") },
                actions = {
                    // Session Status Indicator
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (state.isSessionExpired) Color.Red else Color.Green)
                    )
                    
                    // Dark Mode Toggle
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }

                    // Refresh Button
                    IconButton(onClick = { viewModel.fetchHoldings() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }

                    // Sort Dropdown
                    IconButton(onClick = { sortMenuExpanded = true }) {
                        Icon(Icons.Filled.Sort, contentDescription = "Sort")
                    }
                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sort by LTP") },
                            onClick = {
                                viewModel.updateSortOption(SortOption.LTP)
                                sortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by P&L") },
                            onClick = {
                                viewModel.updateSortOption(SortOption.PNL)
                                sortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by P&L %") },
                            onClick = {
                                viewModel.updateSortOption(SortOption.PNL_PERCENTAGE)
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isSessionExpired) {
                ErrorState(
                    message = state.error ?: "Session expired",
                    buttonText = "Re-Authenticate",
                    onAction = { viewModel.reAuthenticate() }
                )
            } else if (state.error != null && state.holdings.isEmpty()) {
                ErrorState(
                    message = state.error ?: "An error occurred",
                    buttonText = "Retry",
                    onAction = { viewModel.fetchHoldings() }
                )
            } else {
                // Search Bar
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search stocks...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Portfolio Summary
                PortfolioSummaryCard(
                    totalValue = state.totalValue,
                    totalPnl = state.totalPnl
                )

                if (state.isLoading && state.holdings.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (state.filteredHoldings.isEmpty()) {
                    EmptyState(searchQuery = state.searchQuery)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(state.filteredHoldings, key = { it.symbol }) { holding ->
                            StockItemRow(holding = holding, onClick = { onNavigateToDetail(holding.symbol) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PortfolioSummaryCard(totalValue: Double, totalPnl: Double) {
    val pnlColor = if (totalPnl >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
    val pnlSign = if (totalPnl >= 0) "+" else ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Total Portfolio Value",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "₹%.2f".format(totalValue),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Total P&L",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$pnlSign₹%.2f".format(totalPnl),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = pnlColor
            )
        }
    }
}

@Composable
fun StockItemRow(holding: Holding, onClick: () -> Unit) {
    val pnlColor = if (holding.pnl >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
    val pnlSign = if (holding.pnl >= 0) "+" else ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = holding.symbol,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Qty: ${holding.quantity} • Avg: ₹%.2f".format(holding.avgPrice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹%.2f".format(holding.ltp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$pnlSign₹%.2f (${pnlSign}%.2f%%)".format(holding.pnl, holding.pnlPercentage),
                    style = MaterialTheme.typography.bodySmall,
                    color = pnlColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun ErrorState(message: String, buttonText: String, onAction: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAction) {
            Text(buttonText)
        }
    }
}

@Composable
fun EmptyState(searchQuery: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.MoveToInbox,
            contentDescription = "Empty",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        val message = if (searchQuery.isNotEmpty()) {
            "No stocks found matching '$searchQuery'"
        } else {
            "Your portfolio is empty"
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// ---------------------------------------------------------------------------
// TEMPORARY MOCK PREVIEW - Remove or ignore this for production
// ---------------------------------------------------------------------------
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    val mockHoldings = listOf(
        Holding("RELIANCE", "NSE", 10, 2500.0, 2900.0, 4000.0, 16.0),
        Holding("TCS", "NSE", 5, 3200.0, 3100.0, -500.0, -3.12)
    )

    MaterialTheme {
        // We mock the state here directly instead of using the ViewModel
        var sortMenuExpanded by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = { Text("Portfolio") },
                    actions = {
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color.Green)
                        )
                        IconButton(onClick = {}) {
                            Icon(Icons.Filled.DarkMode, contentDescription = "Toggle Theme")
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                        }
                        IconButton(onClick = { sortMenuExpanded = true }) {
                            Icon(Icons.Filled.Sort, contentDescription = "Sort")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    placeholder = { Text("Search stocks...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                PortfolioSummaryCard(totalValue = 44500.0, totalPnl = 3500.0)

                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
                    items(mockHoldings) { holding ->
                        StockItemRow(holding = holding, onClick = {})
                    }
                }
            }
        }
    }
}

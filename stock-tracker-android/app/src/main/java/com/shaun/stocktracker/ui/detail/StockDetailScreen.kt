package com.shaun.stocktracker.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shaun.stocktracker.network.model.Holding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    holding: Holding?,
    viewModel: StockDetailViewModel,
    onNavigateBack: () -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(holding?.symbol?.removeSuffix("-EQ") ?: "Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showBottomSheet = true },
                icon = { Icon(Icons.Default.Notifications, contentDescription = "Alerts") },
                text = { Text("Set / Edit Alert") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (holding == null) {
                Text(
                    text = "Stock not found.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    val pnlColor = if (holding.pnl >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                    val pnlSign = if (holding.pnl >= 0) "+" else ""

                    // Header: Symbol and Exchange
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = holding.symbol.removeSuffix("-EQ"),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = holding.exchange,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // LTP
                    Text(
                        text = "₹%.2f".format(holding.ltp),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    // P&L
                    Text(
                        text = "$pnlSign₹%.2f (${pnlSign}%.2f%%)".format(holding.pnl, holding.pnlPercentage),
                        style = MaterialTheme.typography.titleLarge,
                        color = pnlColor,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Position Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Your Position",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Quantity", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${holding.quantity}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Avg. Price", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("₹%.2f".format(holding.avgPrice), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val invested = holding.quantity * holding.avgPrice
                                val currentValue = holding.quantity * holding.ltp
                                
                                Column {
                                    Text("Invested Value", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("₹%.2f".format(invested), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Current Value", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("₹%.2f".format(currentValue), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Chart Section
                    StockChartSection(holding = holding)
                }
            }
        }
    }

    if (showBottomSheet && holding != null) {
        AlertBottomSheet(
            viewModel = viewModel,
            onDismiss = { showBottomSheet = false },
            currentLtp = holding.ltp
        )
    }
}

@Composable
fun StockChartSection(holding: com.shaun.stocktracker.network.model.Holding) {
    var chartData by remember { mutableStateOf<List<com.github.mikephil.charting.data.Entry>?>(null) }
    var isLoadingChart by remember { mutableStateOf(true) }
    var chartError by remember { mutableStateOf<String?>(null) }
    var selectedRange by remember { mutableStateOf("1D") }
    
    val yahooService = remember { com.shaun.stocktracker.network.YahooFinanceService.create() }

    LaunchedEffect(holding.symbol, selectedRange) {
        val symbol = holding.symbol.removeSuffix("-EQ") + ".NS"
        val (interval, range) = when(selectedRange) {
            "1H" -> "5m" to "1d"
            "3H" -> "15m" to "1d"
            "1D" -> "30m" to "1d"
            "1W" -> "1d" to "5d"
            "1M" -> "1d" to "1mo"
            "3M" -> "1d" to "3mo"
            "1Y" -> "1wk" to "1y"
            else -> "30m" to "1d"
        }

        isLoadingChart = true
        chartError = null
        try {
            val response = yahooService.getChartData(symbol, interval, range)
            val result = response.chart?.result?.firstOrNull()
            val timestamps = result?.timestamp
            val closes = result?.indicators?.quote?.firstOrNull()?.close
            
            if (timestamps != null && closes != null && timestamps.size == closes.size) {
                val entries = mutableListOf<com.github.mikephil.charting.data.Entry>()
                for (i in timestamps.indices) {
                    val closeVal = closes[i]
                    if (closeVal != null) {
                        entries.add(com.github.mikephil.charting.data.Entry(i.toFloat(), closeVal.toFloat()))
                    }
                }
                chartData = entries
            } else {
                chartError = "No chart data available"
            }
        } catch (e: Exception) {
            chartError = "Failed to load chart: ${e.message}"
        } finally {
            isLoadingChart = false
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Price Chart",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Time range buttons
        val ranges = listOf("1H", "3H", "1D", "1W", "1M", "3M", "1Y")
        ScrollableTabRow(
            selectedTabIndex = ranges.indexOf(selectedRange).takeIf { it >= 0 } ?: 2,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            containerColor = Color.Transparent,
            divider = {}
        ) {
            ranges.forEach { range ->
                val selected = range == selectedRange
                Tab(
                    selected = selected,
                    onClick = { selectedRange = range },
                    text = { 
                        Text(
                            range, 
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
                    }
                )
            }
        }

        // Chart Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoadingChart) {
                CircularProgressIndicator()
            } else if (chartError != null) {
                Text(text = chartError ?: "Error", color = MaterialTheme.colorScheme.error)
            } else if (chartData != null) {
                val lineColor = if (holding.ltp >= holding.avgPrice) android.graphics.Color.parseColor("#2E7D32") else android.graphics.Color.parseColor("#C62828")
                androidx.compose.ui.viewinterop.AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        com.github.mikephil.charting.charts.LineChart(context).apply {
                            description.isEnabled = false
                            setTouchEnabled(true)
                            isDragEnabled = true
                            setScaleEnabled(true)
                            setPinchZoom(true)
                            
                            xAxis.apply {
                                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                                setDrawGridLines(false)
                                setDrawLabels(false)
                            }
                            
                            axisLeft.apply {
                                setDrawGridLines(true)
                                textColor = android.graphics.Color.GRAY
                            }
                            
                            axisRight.isEnabled = false
                            legend.isEnabled = false
                        }
                    },
                    update = { chart ->
                        val dataSet = com.github.mikephil.charting.data.LineDataSet(chartData, "Price").apply {
                            color = lineColor
                            setDrawCircles(false)
                            setDrawValues(false)
                            lineWidth = 2f
                            mode = com.github.mikephil.charting.data.LineDataSet.Mode.CUBIC_BEZIER
                            setDrawFilled(true)
                            fillColor = lineColor
                            fillAlpha = 50
                        }
                        chart.data = com.github.mikephil.charting.data.LineData(dataSet)
                        chart.invalidate()
                    }
                )
            }
        }
    }
}

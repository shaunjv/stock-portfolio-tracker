package com.shaun.stocktracker.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.shaun.stocktracker.network.YahooFinanceService
import com.shaun.stocktracker.network.model.Holding
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    holding: Holding?,
    viewModel: StockDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToFullChart: (String) -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(false) }

    // Colors matching the screenshot theme
    val bgColor = Color(0xFF181A20)
    val textColor = Color(0xFFE2E8F0)
    val textSecondary = Color(0xFF94A3B8)
    val borderColor = Color(0xFF2B3139)
    val redColor = Color(0xFFF6465D)
    val greenColor = Color(0xFF0ECB81)

    Scaffold(
        containerColor = bgColor,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showBottomSheet = true },
                icon = { Icon(Icons.Default.Notifications, contentDescription = "Alerts") },
                text = { Text("Set / Edit Alert") },
                containerColor = Color(0xFF2B3139),
                contentColor = textColor
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(bgColor)
        ) {
            if (holding == null) {
                Text(
                    text = "Stock not found.",
                    color = textColor,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    val pnlColor = if (holding.pnl >= 0) greenColor else redColor
                    val isOverallProfit = holding.pnl >= 0
                    
                    // Top Bar Custom Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onNavigateBack, modifier = Modifier.size(28.dp).padding(end = 8.dp)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textSecondary)
                            }
                            Column {
                                Text(
                                    text = holding.symbol.removeSuffix("-EQ"),
                                    color = textColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${holding.symbol.removeSuffix("-EQ")} Limited • ${holding.exchange}",
                                    color = textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "₹%.2f".format(holding.ltp),
                                    color = pnlColor, // Use overall PnL color for price
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = if (holding.pnl >= 0) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = pnlColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            // Hardcoded day change as placeholder since not in Holding
                            Text(
                                text = "-0.00 (-0.00%)",
                                color = textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Chart Section (Outlined Card)
                    Box(modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clickable { onNavigateToFullChart(holding.symbol) }
                    ) {
                        StockChartSection(holding = holding, borderColor = borderColor, textColor = textColor, textSecondary = textSecondary)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Overall Loss/Profit
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isOverallProfit) "Overall Profit" else "Overall Loss",
                                color = textSecondary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Visibility",
                                tint = textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val pnlSign = if (holding.pnl >= 0) "" else "-"
                        Text(
                            text = "$pnlSign₹%.2f (%.2f%%)".format(abs(holding.pnl), holding.pnlPercentage),
                            color = pnlColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stats Grid
                    val invested = holding.quantity * holding.avgPrice
                    val currentValue = holding.quantity * holding.ltp

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            StatItem(
                                label = "Total Quantity",
                                value = "${holding.quantity}",
                                textSecondary = textSecondary,
                                textColor = textColor,
                                modifier = Modifier.weight(1f)
                            )
                            StatItem(
                                label = "Avg Traded Price",
                                value = "₹%.2f".format(holding.avgPrice),
                                textSecondary = textSecondary,
                                textColor = textColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            StatItem(
                                label = "Invested",
                                value = "₹%.0f".format(invested),
                                textSecondary = textSecondary,
                                textColor = textColor,
                                modifier = Modifier.weight(1f)
                            )
                            StatItem(
                                label = "Market Value",
                                value = "₹%.0f".format(currentValue),
                                textSecondary = textSecondary,
                                textColor = textColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            StatItem(
                                label = "Today's Loss",
                                value = "-₹0.00 (-0.00%)",
                                textSecondary = textSecondary,
                                textColor = textColor,
                                modifier = Modifier.weight(1f)
                            )
                            StatItem(
                                label = "Today's Realized Gain",
                                value = "₹0.00",
                                textSecondary = textSecondary,
                                textColor = textColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
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
fun StatItem(label: String, value: String, textSecondary: Color, textColor: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, color = textSecondary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun StockChartSection(holding: Holding, borderColor: Color, textColor: Color, textSecondary: Color) {
    var chartData by remember { mutableStateOf<List<Entry>?>(null) }
    var isLoadingChart by remember { mutableStateOf(true) }
    var chartError by remember { mutableStateOf<String?>(null) }
    var selectedRange by remember { mutableStateOf("1W") }
    
    val yahooService = remember { YahooFinanceService.create() }

    LaunchedEffect(holding.symbol, selectedRange) {
        val symbol = holding.symbol.removeSuffix("-EQ") + ".NS"
        val (interval, range) = when(selectedRange) {
            "1D" -> "5m" to "1d"
            "1W" -> "15m" to "5d"
            "1M" -> "1d" to "1mo"
            "1Y" -> "1wk" to "1y"
            "3Y" -> "1mo" to "3y"
            else -> "1d" to "1mo"
        }

        isLoadingChart = true
        chartError = null
        try {
            val response = yahooService.getChartData(symbol, interval, range)
            val result = response.chart?.result?.firstOrNull()
            val timestamps = result?.timestamp
            val closes = result?.indicators?.quote?.firstOrNull()?.close
            
            if (timestamps != null && closes != null && timestamps.size == closes.size) {
                val entries = mutableListOf<Entry>()
                for (i in timestamps.indices) {
                    val closeVal = closes[i]
                    if (closeVal != null) {
                        entries.add(Entry(i.toFloat(), closeVal.toFloat()))
                    }
                }
                chartData = entries
            } else {
                chartError = "No chart data available"
            }
        } catch (e: Exception) {
            chartError = "Failed to load chart"
        } finally {
            isLoadingChart = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Column {
            // Chart Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoadingChart) {
                    CircularProgressIndicator(color = Color(0xFF0ECB81))
                } else if (chartError != null) {
                    Text(text = chartError ?: "Error", color = Color.Red)
                } else if (chartData != null) {
                    // Teal color like the screenshot
                    val lineColor = android.graphics.Color.parseColor("#00B8D9") 
                    
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            LineChart(context).apply {
                                description.isEnabled = false
                                setTouchEnabled(true)
                                isDragEnabled = true
                                setScaleEnabled(true)
                                setPinchZoom(false)
                                
                                xAxis.apply {
                                    isEnabled = false
                                }
                                
                                axisLeft.apply {
                                    isEnabled = false
                                }
                                
                                axisRight.isEnabled = false
                                legend.isEnabled = false
                                setDrawGridBackground(false)
                                setDrawBorders(false)
                                setViewPortOffsets(0f, 0f, 0f, 0f)
                            }
                        },
                        update = { chart ->
                            val dataSet = LineDataSet(chartData, "Price").apply {
                                color = lineColor
                                setDrawCircles(false)
                                setDrawValues(false)
                                lineWidth = 1.5f
                                mode = LineDataSet.Mode.CUBIC_BEZIER
                                setDrawFilled(true)
                                fillColor = lineColor
                                fillAlpha = 20
                                setDrawHorizontalHighlightIndicator(false)
                                setDrawVerticalHighlightIndicator(true)
                                highLightColor = android.graphics.Color.parseColor("#94A3B8")
                            }
                            chart.data = LineData(dataSet)
                            chart.invalidate()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Tabs
            val ranges = listOf("1D", "1W", "1M", "1Y", "3Y")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ranges.forEach { range ->
                    val selected = range == selectedRange
                    Box(
                        modifier = Modifier
                            .clickable { selectedRange = range }
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = range,
                                color = if (selected) Color(0xFF4C6FFF) else textSecondary,
                                fontSize = 14.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                            if (selected) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .height(2.dp)
                                        .width(20.dp)
                                        .background(Color(0xFF4C6FFF), RoundedCornerShape(1.dp))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

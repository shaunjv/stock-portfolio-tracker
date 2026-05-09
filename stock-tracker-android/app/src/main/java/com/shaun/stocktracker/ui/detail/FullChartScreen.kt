package com.shaun.stocktracker.ui.detail

import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.*
import com.shaun.stocktracker.network.YahooFinanceService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullChartScreen(
    symbol: String,
    currentPrice: Double?,
    onNavigateBack: () -> Unit
) {
    val bgColor = Color(0xFF131722)
    val textColor = Color(0xFFE2E8F0)
    val textSecondary = Color(0xFF94A3B8)
    val redColor = android.graphics.Color.parseColor("#F6465D")
    val greenColor = android.graphics.Color.parseColor("#0ECB81")

    var combinedData by remember { mutableStateOf<CombinedData?>(null) }
    
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedRange by remember { mutableStateOf("1D") }

    val yahooService = remember { YahooFinanceService.create() }

    LaunchedEffect(symbol, selectedRange) {
        val ySymbol = symbol.removeSuffix("-EQ") + ".NS"
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

        isLoading = true
        errorMessage = null
        try {
            val response = yahooService.getChartData(ySymbol, interval, range)
            val result = response.chart?.result?.firstOrNull()
            val timestamps = result?.timestamp
            val quote = result?.indicators?.quote?.firstOrNull()
            
            val opens = quote?.open
            val highs = quote?.high
            val lows = quote?.low
            val closes = quote?.close
            val volumes = quote?.volume
            
            if (timestamps != null && opens != null && highs != null && lows != null && closes != null && volumes != null) {
                val cEntries = mutableListOf<CandleEntry>()
                val vEntries = mutableListOf<BarEntry>()
                val vColors = mutableListOf<Int>()
                
                for (i in timestamps.indices) {
                    val open = opens[i]?.toFloat()
                    val high = highs[i]?.toFloat()
                    val low = lows[i]?.toFloat()
                    val close = closes[i]?.toFloat()
                    val vol = volumes[i]?.toFloat()
                    
                    if (open != null && high != null && low != null && close != null && vol != null) {
                        cEntries.add(CandleEntry(i.toFloat(), high, low, open, close))
                        vEntries.add(BarEntry(i.toFloat(), vol))
                        
                        vColors.add(if (close >= open) greenColor else redColor)
                    }
                }
                
                val candleDataSet = CandleDataSet(cEntries, "Price").apply {
                    setDrawIcons(false)
                    axisDependency = YAxis.AxisDependency.RIGHT
                    shadowColor = android.graphics.Color.DKGRAY
                    shadowWidth = 0.7f
                    decreasingColor = redColor
                    decreasingPaintStyle = Paint.Style.FILL
                    increasingColor = greenColor
                    increasingPaintStyle = Paint.Style.FILL
                    neutralColor = android.graphics.Color.WHITE
                    setDrawValues(false)
                    shadowColorSameAsCandle = true
                }
                
                val barDataSet = BarDataSet(vEntries, "Volume").apply {
                    axisDependency = YAxis.AxisDependency.LEFT
                    colors = vColors
                    setDrawValues(false)
                }
                
                val data = CombinedData()
                data.setData(CandleData(candleDataSet))
                data.setData(BarData(barDataSet))
                
                combinedData = data
            } else {
                errorMessage = "No chart data available"
            }
        } catch (e: Exception) {
            errorMessage = "Failed to load chart: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = bgColor,
        bottomBar = {
            // Time ranges at the bottom
            val ranges = listOf("1H", "3H", "1D", "1W", "1M", "3M", "1Y")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B0E14)) // Slightly darker for bottom bar
                    .padding(horizontal = 8.dp, vertical = 8.dp),
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
                        Text(
                            text = range,
                            color = if (selected) Color(0xFF4C6FFF) else textSecondary,
                            fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(bgColor)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color(0xFF0ECB81), modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(text = errorMessage ?: "Error", color = Color.Red, modifier = Modifier.align(Alignment.Center))
            } else if (combinedData != null) {
                // Combined Chart (Candles + Volume overlapping)
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        CombinedChart(context).apply {
                            description.isEnabled = false
                            setTouchEnabled(true)
                            isDragEnabled = true
                            setScaleEnabled(true)
                            setPinchZoom(true)
                            setDrawGridBackground(false)
                            setDrawBorders(false)

                            xAxis.apply {
                                isEnabled = true
                                position = XAxis.XAxisPosition.BOTTOM
                                setTextColor(android.graphics.Color.GRAY)
                                setDrawGridLines(true)
                                setGridColor(android.graphics.Color.parseColor("#2B3139"))
                                setDrawLabels(false) // Hide labels for clean look
                            }

                            axisLeft.apply {
                                isEnabled = false // Hide volume axis labels
                                axisMinimum = 0f
                                // axisMaximum will be set in update
                            }

                            axisRight.apply {
                                isEnabled = true
                                setTextColor(android.graphics.Color.GRAY)
                                setDrawGridLines(true)
                                setGridColor(android.graphics.Color.parseColor("#2B3139"))
                            }

                            legend.isEnabled = false
                        }
                    },
                    update = { chart ->
                        val maxVol = combinedData?.barData?.yMax ?: 100f
                        // Multiply max volume by 5 so bars stay in the bottom 20%
                        chart.axisLeft.axisMaximum = maxVol * 5f
                        
                        // Limit Line for Current Price
                        chart.axisRight.removeAllLimitLines()
                        if (currentPrice != null) {
                            val limitLine = LimitLine(currentPrice.toFloat(), currentPrice.toString()).apply {
                                lineWidth = 1f
                                enableDashedLine(10f, 10f, 0f)
                                lineColor = redColor
                                setTextColor(android.graphics.Color.WHITE)
                                textSize = 10f
                                labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                            }
                            chart.axisRight.addLimitLine(limitLine)
                        }

                        chart.data = combinedData
                        chart.invalidate()
                    }
                )

                // Overlay Text (Top Left)
                Column(modifier = Modifier.align(Alignment.TopStart).padding(top = 16.dp, start = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigateBack, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textSecondary)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${symbol.removeSuffix("-EQ")} • NSE",
                            color = textColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (currentPrice != null) {
                        Text(
                            text = "%.2f  0.00 (0.00%%)".format(currentPrice), // Placeholder day change
                            color = Color(0xFF0ECB81), // Green text like screenshot
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 36.dp, top = 4.dp)
                        )
                    }
                    Text(
                        text = "Volume",
                        color = textSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 36.dp, top = 8.dp)
                    )
                    Text(
                        text = "MA 44 close 0",
                        color = textSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 36.dp, top = 4.dp)
                    )
                }
            }
        }
    }
}

package com.shaun.stocktracker.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shaun.stocktracker.data.AppDatabase
import com.shaun.stocktracker.data.entity.AlertCondition
import com.shaun.stocktracker.data.entity.AlertType
import com.shaun.stocktracker.repository.AlertRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for StockDetailScreen.
 * Scoped to a specific stock symbol.
 * Manages the alerts list shown in the bottom sheet for this stock.
 */
class StockDetailViewModel(
    application: Application,
    val symbol: String,
    private val alertRepository: AlertRepository
) : AndroidViewModel(application) {

    private val _alerts = MutableStateFlow<List<AlertCondition>>(emptyList())
    val alerts: StateFlow<List<AlertCondition>> = _alerts.asStateFlow()

    init {
        loadAlerts()
    }

    private fun loadAlerts() {
        viewModelScope.launch {
            // Load alerts specifically for this symbol
            _alerts.value = alertRepository.getAlertsBySymbol(symbol)
        }
    }

    fun addAlert(alertType: AlertType, targetPrice: Double) {
        viewModelScope.launch {
            alertRepository.createAlert(
                tradingSymbol = symbol,
                alertType = alertType,
                targetPrice = targetPrice
            )
            loadAlerts()
        }
    }

    fun deleteAlert(alert: AlertCondition) {
        viewModelScope.launch {
            alertRepository.deleteAlert(alert)
            loadAlerts()
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val symbol: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StockDetailViewModel::class.java)) {
                val db = AppDatabase.getInstance(application)
                val repository = AlertRepository(db.alertDao(), db.alertHistoryDao())
                return StockDetailViewModel(application, symbol, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

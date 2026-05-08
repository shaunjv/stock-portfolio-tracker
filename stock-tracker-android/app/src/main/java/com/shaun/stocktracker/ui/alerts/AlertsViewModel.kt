package com.shaun.stocktracker.ui.alerts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shaun.stocktracker.data.AppDatabase
import com.shaun.stocktracker.data.entity.AlertCondition
import com.shaun.stocktracker.data.entity.AlertHistory
import com.shaun.stocktracker.data.entity.AlertType
import com.shaun.stocktracker.repository.AlertRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Alerts Tab.
 * Manages alert list state, filtering, and CRUD operations.
 */
class AlertsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AlertRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = AlertRepository(db.alertDao(), db.alertHistoryDao())
    }

    // ── State ─────────────────────────────────────────────────

    private val _alerts = MutableStateFlow<List<AlertCondition>>(emptyList())
    val alerts: StateFlow<List<AlertCondition>> = _alerts.asStateFlow()

    private val _history = MutableStateFlow<List<AlertHistory>>(emptyList())
    val history: StateFlow<List<AlertHistory>> = _history.asStateFlow()

    private val _currentFilter = MutableStateFlow(AlertFilter.ALL)
    val currentFilter: StateFlow<AlertFilter> = _currentFilter.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Alert being edited (null = create mode)
    private val _editingAlert = MutableStateFlow<AlertCondition?>(null)
    val editingAlert: StateFlow<AlertCondition?> = _editingAlert.asStateFlow()

    init {
        loadAlerts()
    }

    // ── Filtering ─────────────────────────────────────────────

    fun setFilter(filter: AlertFilter) {
        _currentFilter.value = filter
        loadAlerts()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        loadAlerts()
    }

    fun loadAlerts() {
        viewModelScope.launch {
            _isLoading.value = true
            val all = when (_currentFilter.value) {
                AlertFilter.ALL -> repository.getAllAlerts()
                AlertFilter.ACTIVE -> repository.getActiveAlerts()
                AlertFilter.TRIGGERED -> repository.getTriggeredAlerts()
                AlertFilter.DISABLED -> repository.getDisabledAlerts()
            }

            val query = _searchQuery.value.trim().uppercase()
            _alerts.value = if (query.isNotEmpty()) {
                all.filter { it.tradingSymbol.contains(query) }
            } else {
                all
            }
            _isLoading.value = false
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            _history.value = repository.getAlertHistory()
        }
    }

    // ── CRUD ──────────────────────────────────────────────────

    fun createAlert(symbol: String, alertType: AlertType, targetPrice: Double) {
        viewModelScope.launch {
            repository.createAlert(
                tradingSymbol = symbol.trim().uppercase(),
                alertType = alertType,
                targetPrice = targetPrice
            )
            loadAlerts()
        }
    }

    fun updateAlert(alert: AlertCondition) {
        viewModelScope.launch {
            repository.updateAlert(alert)
            loadAlerts()
        }
    }

    fun enableAlert(id: Long) {
        viewModelScope.launch {
            repository.enableAlert(id)
            loadAlerts()
        }
    }

    fun disableAlert(id: Long) {
        viewModelScope.launch {
            repository.disableAlert(id)
            loadAlerts()
        }
    }

    fun toggleAlert(alert: AlertCondition) {
        if (alert.isEnabled) disableAlert(alert.id) else enableAlert(alert.id)
    }

    fun deleteAlert(id: Long) {
        viewModelScope.launch {
            repository.deleteAlertById(id)
            loadAlerts()
        }
    }

    // ── Edit mode ─────────────────────────────────────────────

    fun startEditing(alert: AlertCondition) {
        _editingAlert.value = alert
    }

    fun clearEditing() {
        _editingAlert.value = null
    }
}

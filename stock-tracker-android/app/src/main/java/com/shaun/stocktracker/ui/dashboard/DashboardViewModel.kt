package com.shaun.stocktracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shaun.stocktracker.network.model.Holding
import com.shaun.stocktracker.repository.AuthRepository
import com.shaun.stocktracker.repository.PortfolioRepository
import com.shaun.stocktracker.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SortOption {
    LTP, PNL, PNL_PERCENTAGE
}

data class DashboardState(
    val isLoading: Boolean = false,
    val holdings: List<Holding> = emptyList(),
    val filteredHoldings: List<Holding> = emptyList(),
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.LTP,
    val error: String? = null,
    val isSessionExpired: Boolean = false,
    val totalValue: Double = 0.0,
    val totalPnl: Double = 0.0
)

class DashboardViewModel(
    private val portfolioRepository: PortfolioRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        fetchHoldings()
    }

    fun fetchHoldings() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val status = authRepository.isSessionActive()
            if (status is NetworkResult.Success && !status.data) {
                _state.update { it.copy(isLoading = false, isSessionExpired = true, error = "Session expired. Please re-authenticate.") }
                return@launch
            }
            
            when (val result = portfolioRepository.fetchHoldings()) {
                is NetworkResult.Success -> {
                    val holdings = result.data
                    val totalValue = holdings.sumOf { it.ltp * it.quantity }
                    val totalPnl = holdings.sumOf { it.pnl }
                    
                    _state.update {
                        it.copy(
                            isLoading = false,
                            holdings = holdings,
                            isSessionExpired = false,
                            totalValue = totalValue,
                            totalPnl = totalPnl,
                            error = null
                        )
                    }
                    applyFiltersAndSort()
                }
                is NetworkResult.Unauthorized -> {
                    _state.update { 
                        it.copy(isLoading = false, isSessionExpired = true, error = result.message ?: "Session expired")
                    }
                }
                is NetworkResult.Error -> {
                    _state.update { 
                        it.copy(isLoading = false, error = result.message ?: "Failed to fetch portfolio")
                    }
                }
                is NetworkResult.Loading -> {
                    _state.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun reAuthenticate() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.login()) {
                is NetworkResult.Success -> {
                    fetchHoldings()
                }
                is NetworkResult.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {
                    _state.update { it.copy(isLoading = false, error = "Unknown error during authentication") }
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        applyFiltersAndSort()
    }

    fun updateSortOption(option: SortOption) {
        _state.update { it.copy(sortOption = option) }
        applyFiltersAndSort()
    }

    private fun applyFiltersAndSort() {
        val currentState = _state.value
        var filtered = currentState.holdings

        if (currentState.searchQuery.isNotBlank()) {
            val query = currentState.searchQuery.lowercase()
            filtered = filtered.filter { 
                it.symbol.lowercase().contains(query)
            }
        }

        filtered = when (currentState.sortOption) {
            SortOption.LTP -> filtered.sortedByDescending { it.ltp }
            SortOption.PNL -> filtered.sortedByDescending { it.pnl }
            SortOption.PNL_PERCENTAGE -> filtered.sortedByDescending { it.pnlPercentage }
        }

        _state.update { it.copy(filteredHoldings = filtered) }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val portfolioRepository: PortfolioRepository,
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                return DashboardViewModel(portfolioRepository, authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

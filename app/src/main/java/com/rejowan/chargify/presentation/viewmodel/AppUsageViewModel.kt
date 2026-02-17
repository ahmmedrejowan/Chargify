package com.rejowan.chargify.presentation.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejowan.chargify.data.model.AppUsageInfo
import com.rejowan.chargify.data.repository.AppUsageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TimeRange {
    TODAY, LAST_24H, LAST_7D
}

data class AppUsageUiState(
    val isLoading: Boolean = true,
    val hasPermission: Boolean = false,
    val appUsageList: List<AppUsageInfo> = emptyList(),
    val totalScreenTime: Long = 0L,
    val selectedTimeRange: TimeRange = TimeRange.TODAY,
    val error: String? = null
)

class AppUsageViewModel(
    private val appUsageRepository: AppUsageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppUsageUiState())
    val uiState: StateFlow<AppUsageUiState> = _uiState.asStateFlow()

    init {
        checkPermissionAndLoad()
    }

    fun checkPermissionAndLoad() {
        viewModelScope.launch(Dispatchers.IO) {
            val hasPermission = appUsageRepository.hasUsageStatsPermission()
            _uiState.value = _uiState.value.copy(
                hasPermission = hasPermission,
                isLoading = hasPermission
            )

            if (hasPermission) {
                loadUsageStats()
            }
        }
    }

    fun loadUsageStats() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val stats = when (_uiState.value.selectedTimeRange) {
                    TimeRange.TODAY -> appUsageRepository.getTodayUsageStats()
                    TimeRange.LAST_24H -> appUsageRepository.getAppUsageStats(24 * 60 * 60 * 1000L)
                    TimeRange.LAST_7D -> appUsageRepository.getWeekUsageStats()
                }

                val totalTime = stats.sumOf { it.usageTimeMs }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    appUsageList = stats,
                    totalScreenTime = totalTime
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun setTimeRange(timeRange: TimeRange) {
        if (_uiState.value.selectedTimeRange != timeRange) {
            _uiState.value = _uiState.value.copy(selectedTimeRange = timeRange)
            loadUsageStats()
        }
    }

    fun getUsageStatsSettingsIntent(): Intent {
        return appUsageRepository.getUsageStatsSettingsIntent()
    }

    fun formatTotalScreenTime(timeMs: Long): String {
        val hours = timeMs / (1000 * 60 * 60)
        val minutes = (timeMs % (1000 * 60 * 60)) / (1000 * 60)
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "0m"
        }
    }
}

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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DayOption(
    val date: Date,
    val label: String,
    val isToday: Boolean = false,
    val isYesterday: Boolean = false
)

data class AppUsageUiState(
    val isLoading: Boolean = true,
    val hasPermission: Boolean = false,
    val appUsageList: List<AppUsageInfo> = emptyList(),
    val totalScreenTime: Long = 0L,
    val availableDays: List<DayOption> = emptyList(),
    val selectedDay: DayOption? = null,
    val error: String? = null
)

class AppUsageViewModel(
    private val appUsageRepository: AppUsageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppUsageUiState())
    val uiState: StateFlow<AppUsageUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())

    init {
        checkPermissionAndLoad()
    }

    fun checkPermissionAndLoad() {
        viewModelScope.launch(Dispatchers.IO) {
            val hasPermission = appUsageRepository.hasUsageStatsPermission()

            // Generate available days (today + last 6 days = 7 days total)
            val days = generateAvailableDays()

            _uiState.value = _uiState.value.copy(
                hasPermission = hasPermission,
                isLoading = hasPermission,
                availableDays = days,
                selectedDay = days.firstOrNull()
            )

            if (hasPermission) {
                loadUsageStats()
            }
        }
    }

    private fun generateAvailableDays(): List<DayOption> {
        val days = mutableListOf<DayOption>()
        val calendar = Calendar.getInstance()

        // Reset to start of today
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        for (i in 0 until 7) {
            val date = calendar.time
            val label = when (i) {
                0 -> "Today"
                1 -> "Yesterday"
                else -> dateFormat.format(date)
            }

            days.add(
                DayOption(
                    date = date,
                    label = label,
                    isToday = i == 0,
                    isYesterday = i == 1
                )
            )

            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        return days
    }

    fun loadUsageStats() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val selectedDay = _uiState.value.selectedDay
                val stats = if (selectedDay != null) {
                    appUsageRepository.getUsageStatsForDay(selectedDay.date)
                } else {
                    appUsageRepository.getTodayUsageStats()
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

    fun selectDay(day: DayOption) {
        if (_uiState.value.selectedDay != day) {
            _uiState.value = _uiState.value.copy(selectedDay = day)
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

    fun getSelectedDayFullLabel(): String {
        val selectedDay = _uiState.value.selectedDay ?: return "Today"
        return when {
            selectedDay.isToday -> "Today"
            selectedDay.isYesterday -> "Yesterday"
            else -> fullDateFormat.format(selectedDay.date)
        }
    }
}

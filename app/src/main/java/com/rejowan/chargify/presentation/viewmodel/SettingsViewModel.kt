package com.rejowan.chargify.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejowan.chargify.data.preferences.ThemeMode
import com.rejowan.chargify.data.preferences.ThemePreferences
import com.rejowan.chargify.data.repository.ChargingHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class SettingsUiState(
    val chargingSessionCount: Int = 0,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
)

class SettingsViewModel(
    private val chargingHistoryRepository: ChargingHistoryRepository,
    private val themePreferences: ThemePreferences
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        loadStats()
        observeThemeSettings()
    }

    private fun loadStats() {
        viewModelScope.launch {
            chargingHistoryRepository.getAllSessions().collect { sessions ->
                _state.value = _state.value.copy(chargingSessionCount = sessions.size)
            }
        }
    }

    private fun observeThemeSettings() {
        viewModelScope.launch {
            combine(
                themePreferences.themeMode,
                themePreferences.dynamicColorEnabled
            ) { themeMode, dynamicColor ->
                Pair(themeMode, dynamicColor)
            }.collect { (themeMode, dynamicColor) ->
                _state.value = _state.value.copy(
                    themeMode = themeMode,
                    dynamicColorEnabled = dynamicColor
                )
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        themePreferences.setThemeMode(mode)
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        themePreferences.setDynamicColorEnabled(enabled)
    }

    fun clearChargingHistory() {
        viewModelScope.launch {
            try {
                chargingHistoryRepository.clearHistory()
                _state.value = _state.value.copy(
                    successMessage = "Charging history cleared",
                    chargingSessionCount = 0
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Failed to clear history")
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(successMessage = null, error = null)
    }
}

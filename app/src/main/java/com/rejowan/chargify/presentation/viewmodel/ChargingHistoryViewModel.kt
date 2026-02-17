package com.rejowan.chargify.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejowan.chargify.data.local.ChargingSession
import com.rejowan.chargify.data.repository.ChargingHistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class ChargingHistoryViewModel(
    private val repository: ChargingHistoryRepository
) : ViewModel() {

    val sessions: StateFlow<List<ChargingSession>> = repository.getRecentSessions(100)
        .onEach { sessions ->
            Timber.tag("ChargeHistory").d("ViewModel received ${sessions.size} sessions from database")
            sessions.forEach { session ->
                Timber.tag("ChargeHistory").d("  - Session: ${session.startLevel}% -> ${session.endLevel}%, charging=${session.isCharging}")
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteSession(id: Long) {
        viewModelScope.launch {
            repository.deleteSession(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}

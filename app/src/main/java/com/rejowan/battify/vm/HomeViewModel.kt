package com.rejowan.battify.vm


import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rejowan.battify.repo.HomeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: HomeRepository) : ViewModel() {

    val chargeLevel: StateFlow<Int?> = repository.chargeLevel
    val isCharging: StateFlow<Boolean?> = repository.isCharging

    private val _currentUsage = repository.getCurrentUsage()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val currentUsage: StateFlow<Float?> get() = _currentUsage

    fun getBatteryInfoFromIntent(intent: Intent) {
        repository.getBatteryInfoFromIntent(intent)
    }
}

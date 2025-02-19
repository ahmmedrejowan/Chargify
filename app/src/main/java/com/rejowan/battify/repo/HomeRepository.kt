package com.rejowan.battify.repo

import android.content.Intent
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow

interface HomeRepository {

    val chargeLevel: StateFlow<Int?>
    val isCharging: StateFlow<Boolean?>
    val batterTemp: StateFlow<Pair<Float, Float>?>
    val voltage : StateFlow<Float?>

    fun getBatteryInfoFromIntent(intent: Intent)

    fun getCurrentUsage(): Flow<Float?>


}

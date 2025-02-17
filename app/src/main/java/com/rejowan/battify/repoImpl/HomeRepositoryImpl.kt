package com.rejowan.battify.repoImpl

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import com.rejowan.battify.repo.HomeRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import java.text.DecimalFormat
import kotlin.math.abs

class HomeRepositoryImpl(private val context: Context) : HomeRepository {

    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    private val _chargeLevel = MutableStateFlow<Int?>(null)
    override val chargeLevel = _chargeLevel.asStateFlow()

    private val _isCharging = MutableStateFlow<Boolean?>(null)
    override val isCharging = _isCharging.asStateFlow()

    override fun getBatteryInfoFromIntent(intent: Intent) {
        val deviceStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        _isCharging.value =
            deviceStatus == BatteryManager.BATTERY_STATUS_CHARGING || deviceStatus == BatteryManager.BATTERY_STATUS_FULL

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = level / scale.toFloat()
        _chargeLevel.value = (DecimalFormat("#.##").format(batteryPct * 100)).toInt()
    }

    override fun getCurrentUsage(): Flow<Float?> = flow {
        while (true) {
            var batteryCurrent = -batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW).toFloat()

            val usage = if (batteryCurrent < 0) {
                if (abs(batteryCurrent / 1000) < 1.0) {
                    batteryCurrent *= 1000
                }
                DecimalFormat("#.##").format((batteryCurrent / 1000).toDouble()).toFloat()
            } else {
                if (abs(batteryCurrent) > 100000.0) {
                    batteryCurrent /= 1000
                }
                DecimalFormat("#.##").format(batteryCurrent.toDouble()).toFloat()
            }

            emit(usage)
            delay(1000) // Poll every second
        }
    }
}

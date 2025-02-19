package com.rejowan.battify.repoImpl

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log
import com.rejowan.battify.repo.HomeRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.abs

class HomeRepositoryImpl(private val context: Context) : HomeRepository {

    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    private val _chargeLevel = MutableStateFlow<Int?>(null)
    override val chargeLevel = _chargeLevel.asStateFlow()

    private val _isCharging = MutableStateFlow<Boolean?>(null)
    override val isCharging = _isCharging.asStateFlow()

    private val _batteryTemp = MutableStateFlow<Pair<Float, Float>?>(null)
    override val batterTemp: StateFlow<Pair<Float, Float>?>
        get() = _batteryTemp.asStateFlow()

    private val _voltage = MutableStateFlow<Float?>(null)
    override val voltage: StateFlow<Float?>
        get() = _voltage.asStateFlow()

    override fun getBatteryInfoFromIntent(intent: Intent) {
        val deviceStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        _isCharging.value =
            deviceStatus == BatteryManager.BATTERY_STATUS_CHARGING || deviceStatus == BatteryManager.BATTERY_STATUS_FULL

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = level / scale.toFloat()
        _chargeLevel.value = (DecimalFormat("#.##").format(batteryPct * 100)).toInt()

        val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        val tempInCelsius = temp / 10.0f
        val tempInFahrenheit = (tempInCelsius * 9 / 5) + 32
        _batteryTemp.value = Pair(tempInCelsius, tempInFahrenheit)

        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        val formattedVoltage = DecimalFormat("#.##").format(voltage / 1000.0f)
        _voltage.value = formattedVoltage.toFloat()

    }

    override fun getCurrentUsage(): Flow<Float> = flow {
        while (true) {
            val rawValue = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            Log.e("rawValue", "Raw Value $rawValue")

            val usage = if (abs(rawValue / 1000) < 1.0) {
                rawValue * 1.0f
            } else {
                rawValue / 1000.0f
            }

            val formattedUsage = usage.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toFloat()
            emit(formattedUsage)

            delay(1000)
        }
    }

}

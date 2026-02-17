package com.rejowan.chargify.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.rejowan.chargify.data.local.ChargingSession
import com.rejowan.chargify.data.model.BatteryState
import com.rejowan.chargify.data.model.ChargingStatus
import com.rejowan.chargify.data.model.PowerSource
import com.rejowan.chargify.data.model.SessionStats
import com.rejowan.chargify.domain.util.BatteryCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.abs

class BatteryRepositoryImpl(
    private val context: Context,
    private val chargingHistoryRepository: ChargingHistoryRepository
) : BatteryRepository {

    private val batteryManager =
        context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var monitoringJob: Job? = null

    private val _batteryState = MutableStateFlow(BatteryState())
    override val batteryState: StateFlow<BatteryState> = _batteryState.asStateFlow()

    private val _currentUsageHistory = MutableStateFlow<List<Float>>(emptyList())
    override val currentUsageHistory: StateFlow<List<Float>> = _currentUsageHistory.asStateFlow()

    private val _temperatureHistory = MutableStateFlow<List<Float>>(emptyList())
    override val temperatureHistory: StateFlow<List<Float>> = _temperatureHistory.asStateFlow()

    private val _voltageHistory = MutableStateFlow<List<Float>>(emptyList())
    override val voltageHistory: StateFlow<List<Float>> = _voltageHistory.asStateFlow()

    private val _batteryLevelHistory = MutableStateFlow<List<Float>>(emptyList())
    override val batteryLevelHistory: StateFlow<List<Float>> = _batteryLevelHistory.asStateFlow()

    private val _powerHistory = MutableStateFlow<List<Float>>(emptyList())
    override val powerHistory: StateFlow<List<Float>> = _powerHistory.asStateFlow()

    private val _sessionStats = MutableStateFlow(SessionStats())
    override val sessionStats: StateFlow<SessionStats> = _sessionStats.asStateFlow()

    private val currentUsageSamples = mutableListOf<Float>()
    private var batteryCapacityMah: Int? = null
    private var lastChargingState: Boolean? = null

    // Session tracking for history
    private var sessionStartTime: Long = 0L
    private var sessionStartLevel: Int = 0
    private var sessionPowerSource: String = ""
    private val sessionCurrentSamples = mutableListOf<Float>()
    private val sessionTempSamples = mutableListOf<Float>()

    init {
        fetchBatteryCapacity()
    }

    override fun processIntent(intent: Intent) {
        val deviceStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val chargingStatus = ChargingStatus.fromBatteryStatus(deviceStatus)
        val isCharging = chargingStatus == ChargingStatus.CHARGING || chargingStatus == ChargingStatus.FULL

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val chargeLevel = if (scale > 0) {
            (DecimalFormat("#.##").format(level / scale.toFloat() * 100)).toInt()
        } else 0

        val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        val tempCelsius = temp / 10.0f
        val tempFahrenheit = (tempCelsius * 9 / 5) + 32

        val voltageRaw = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        val voltage = DecimalFormat("#.##").format(voltageRaw / 1000.0f).toFloat()

        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val powerSource = PowerSource.fromPluggedValue(plugged)

        val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

        _batteryState.value = _batteryState.value.copy(
            chargeLevel = chargeLevel,
            isCharging = isCharging,
            chargingStatus = chargingStatus,
            powerSource = powerSource,
            temperatureCelsius = tempCelsius,
            temperatureFahrenheit = tempFahrenheit,
            voltage = voltage,
            batteryCapacityMah = batteryCapacityMah,
            batteryHealth = health,
            batteryTechnology = technology
        )

        // Detect charging state change -> save previous session and start new one
        if (lastChargingState != null && lastChargingState != isCharging) {
            Timber.tag("ChargeHistory").d("[IN-APP] Charging state changed: $lastChargingState -> $isCharging")

            // Save the previous session if it was meaningful (at least 1 minute and level changed)
            val sessionDuration = System.currentTimeMillis() - sessionStartTime
            val levelDiff = chargeLevel - sessionStartLevel
            Timber.tag("ChargeHistory").d("[IN-APP] Session check: duration=${sessionDuration}ms, levelDiff=$levelDiff, samples=${sessionCurrentSamples.size}")

            if (sessionStartTime > 0 && sessionDuration > 60_000 && levelDiff != 0) {
                val avgCurrent = if (sessionCurrentSamples.isNotEmpty()) {
                    sessionCurrentSamples.map { abs(it) }.average().toFloat()
                } else 0f
                val avgTemp = if (sessionTempSamples.isNotEmpty()) {
                    sessionTempSamples.average().toFloat()
                } else 0f

                Timber.tag("ChargeHistory").d("[IN-APP] SAVING SESSION: $sessionStartLevel% -> $chargeLevel%")
                scope.launch {
                    try {
                        chargingHistoryRepository.addSession(
                            ChargingSession(
                                startTime = sessionStartTime,
                                endTime = System.currentTimeMillis(),
                                startLevel = sessionStartLevel,
                                endLevel = chargeLevel,
                                isCharging = lastChargingState ?: false,
                                powerSource = sessionPowerSource,
                                averageCurrentMa = avgCurrent,
                                averageTempCelsius = avgTemp
                            )
                        )
                        Timber.tag("ChargeHistory").d("[IN-APP] SESSION SAVED SUCCESSFULLY!")
                    } catch (e: Exception) {
                        Timber.tag("ChargeHistory").e(e, "[IN-APP] FAILED TO SAVE SESSION!")
                    }
                }
            } else {
                Timber.tag("ChargeHistory").d("[IN-APP] Session NOT saved: duration=${sessionDuration}ms, levelDiff=$levelDiff")
            }

            // Start new session
            sessionStartTime = System.currentTimeMillis()
            sessionStartLevel = chargeLevel
            sessionPowerSource = powerSource.displayName()
            sessionCurrentSamples.clear()
            sessionTempSamples.clear()

            _sessionStats.value = SessionStats(
                isCharging = isCharging,
                startLevel = chargeLevel,
                currentLevel = chargeLevel
            )
        } else {
            // First time initialization
            if (lastChargingState == null) {
                Timber.tag("ChargeHistory").d("[IN-APP] First initialization at $chargeLevel%")
                sessionStartTime = System.currentTimeMillis()
                sessionStartLevel = chargeLevel
                sessionPowerSource = powerSource.displayName()
            }

            _sessionStats.value = _sessionStats.value.copy(
                isCharging = isCharging,
                currentLevel = chargeLevel
            )
        }
        lastChargingState = isCharging
    }

    override fun startMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = scope.launch {
            while (isActive) {
                pollCurrentUsage()
                pollBatteryIntent()
                delay(1000)
            }
        }
    }

    override fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
    }

    private fun pollCurrentUsage() {
        val rawValue = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        Timber.d("Raw current value: $rawValue")

        val usage = if (abs(rawValue / 1000) < 1.0) {
            rawValue * 1.0f
        } else {
            rawValue / 1000.0f
        }

        val formattedUsage = usage.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toFloat()

        // Get average current
        val rawAvg = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
        val avgUsage = if (abs(rawAvg / 1000) < 1.0) {
            rawAvg * 1.0f
        } else {
            rawAvg / 1000.0f
        }
        val formattedAvgUsage = avgUsage.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toFloat()

        // Get charge counter (remaining capacity in µAh, convert to mAh)
        val chargeCounterUah = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        val chargeCounterMah = if (chargeCounterUah > 0) {
            (chargeCounterUah / 1000.0f).toBigDecimal().setScale(1, RoundingMode.HALF_UP).toFloat()
        } else null

        // Get energy counter (remaining energy in nWh, convert to Wh)
        val energyCounterNwh = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)
        val energyCounterWh = if (energyCounterNwh > 0) {
            (energyCounterNwh / 1_000_000_000.0f).toBigDecimal().setScale(2, RoundingMode.HALF_UP).toFloat()
        } else null

        // Get cycle count (Android 14+, API 34)
        val cycleCount = if (android.os.Build.VERSION.SDK_INT >= 34) {
            try {
                val cycleCountProperty = 6 // BATTERY_PROPERTY_CHARGING_CYCLE_COUNT
                val count = batteryManager.getIntProperty(cycleCountProperty)
                if (count > 0) count else null
            } catch (e: Exception) {
                Timber.e(e, "Failed to get cycle count")
                null
            }
        } else null

        _batteryState.value = _batteryState.value.copy(
            currentUsageMa = formattedUsage,
            currentAverageMa = formattedAvgUsage,
            chargeCounterMah = chargeCounterMah,
            energyCounterWh = energyCounterWh,
            cycleCount = cycleCount
        )
        addToHistory(_currentUsageHistory, formattedUsage)

        // Calculate power in watts: V * mA / 1000
        val state = _batteryState.value
        val powerWatts = (state.voltage * abs(formattedUsage)) / 1000f
        val formattedPower = powerWatts.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toFloat()
        addToHistory(_powerHistory, formattedPower)

        // Update session stats with current sample
        val session = _sessionStats.value
        _sessionStats.value = session.copy(
            sampleCount = session.sampleCount + 1,
            totalCurrentMa = session.totalCurrentMa + abs(formattedUsage)
        )

        // Track for history logging
        sessionCurrentSamples.add(formattedUsage)

        // Collect samples for time estimation
        currentUsageSamples.add(formattedUsage)
        if (currentUsageSamples.size >= 5) {
            calculateTimeEstimates()
            currentUsageSamples.clear()
        }
    }

    private fun pollBatteryIntent() {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return

        val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        val tempCelsius = temp / 10.0f
        val tempFahrenheit = (tempCelsius * 9 / 5) + 32

        val voltageRaw = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        val voltage = DecimalFormat("#.##").format(voltageRaw / 1000.0f).toFloat()

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val chargeLevel = if (scale > 0) {
            (DecimalFormat("#.##").format(level / scale.toFloat() * 100)).toInt()
        } else _batteryState.value.chargeLevel

        _batteryState.value = _batteryState.value.copy(
            temperatureCelsius = tempCelsius,
            temperatureFahrenheit = tempFahrenheit,
            voltage = voltage,
            chargeLevel = chargeLevel
        )

        addToHistory(_temperatureHistory, tempCelsius)
        addToHistory(_voltageHistory, voltage)
        addToHistory(_batteryLevelHistory, chargeLevel.toFloat())

        // Track for history logging
        sessionTempSamples.add(tempCelsius)
    }

    private fun calculateTimeEstimates() {
        if (!BatteryCalculator.areSamplesConsistent(currentUsageSamples)) {
            Timber.d("Samples inconsistent, discarding")
            currentUsageSamples.clear()
            return
        }

        val avgUsage = currentUsageSamples.map { abs(it.toDouble()) }.average()
        val state = _batteryState.value
        val capacity = batteryCapacityMah ?: return

        if (state.isCharging) {
            val eta = BatteryCalculator.calculateEtaToFull(state.chargeLevel, capacity, avgUsage)
            _batteryState.value = _batteryState.value.copy(
                etaToFullMillis = eta,
                timeRemainingMillis = null
            )
            Timber.d("ETA to full: $eta ms")
        } else {
            val remaining = BatteryCalculator.calculateTimeRemaining(state.chargeLevel, capacity, avgUsage)
            _batteryState.value = _batteryState.value.copy(
                etaToFullMillis = null,
                timeRemainingMillis = remaining
            )
            Timber.d("Time remaining: $remaining ms")
        }
    }

    @SuppressLint("PrivateApi")
    private fun fetchBatteryCapacity() {
        try {
            val ppClass = Class.forName("com.android.internal.os.PowerProfile")
            val ctor = ppClass.getConstructor(Context::class.java)
            val pp = ctor.newInstance(context)
            val method = ppClass.getMethod("getBatteryCapacity")
            val capMah = method.invoke(pp) as? Double
            batteryCapacityMah = if (capMah != null && capMah > 0) capMah.toInt() else null
            Timber.d("Battery capacity: $batteryCapacityMah mAh")
        } catch (e: Exception) {
            Timber.e(e, "Failed to get battery capacity")
            batteryCapacityMah = null
        }
    }

    private fun addToHistory(historyFlow: MutableStateFlow<List<Float>>, value: Float) {
        val current = historyFlow.value.toMutableList()
        current.add(value)
        if (current.size > 25) {
            current.removeAt(0)
        }
        historyFlow.value = current
    }
}

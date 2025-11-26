package com.example.liftium.model

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * State model for step counter
 */
data class StepCounterState(
    val totalSteps: Int = 0,
    val dailySteps: Int = 0,
    val isSensorAvailable: Boolean = false,
    val isListening: Boolean = false,
    val lastResetTimestamp: Long = System.currentTimeMillis()
)

/**
 * ViewModel for managing step counter sensor
 *
 * Current Implementation: Real-time sensor data with in-memory storage
 * Future: Will integrate with database for daily step history
 *
 * Uses TYPE_STEP_COUNTER which provides cumulative steps since last reboot
 */
class StepCounterViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val _state = MutableStateFlow(StepCounterState())
    val state: StateFlow<StepCounterState> = _state.asStateFlow()

    private var initialStepCount: Int = 0
    private var hasInitialCount: Boolean = false

    // Mock mode for testing on emulators
    private var mockStepJob: Job? = null
    private var mockStepCount: Int = 0

    companion object {
        private const val TAG = "StepCounterViewModel"
        private const val ENABLE_MOCK_MODE = true // Set to false to disable mock testing
        private const val MOCK_STEP_INCREMENT = 50 // Steps added per interval
        private const val MOCK_STEP_INTERVAL_MS = 3000L // Update every 3 seconds
    }

    init {
        val isSensorAvailable = stepSensor != null
        _state.value = _state.value.copy(isSensorAvailable = isSensorAvailable)

        if (isSensorAvailable) {
            Log.d(TAG, "Step counter sensor available")
            startListening()
        } else {
            Log.w(TAG, "Step counter sensor NOT available on this device")

            // Enable mock mode for testing
            if (ENABLE_MOCK_MODE) {
                Log.d(TAG, "MOCK MODE ENABLED - Simulating steps for testing")
                startMockMode()
            }
        }
    }

    /**
     * Start listening to step counter sensor
     */
    fun startListening() {
        if (stepSensor == null) {
            Log.w(TAG, "Cannot start listening - sensor not available")
            return
        }

        val registered = sensorManager.registerListener(
            this,
            stepSensor,
            SensorManager.SENSOR_DELAY_UI
        )

        if (registered) {
            _state.value = _state.value.copy(isListening = true)
            Log.d(TAG, "Started listening to step counter")
        } else {
            Log.e(TAG, "Failed to register sensor listener")
        }
    }

    /**
     * Stop listening to step counter sensor
     */
    fun stopListening() {
        if (stepSensor == null) return

        sensorManager.unregisterListener(this)
        _state.value = _state.value.copy(isListening = false)
        Log.d(TAG, "Stopped listening to step counter")
    }

    /**
     * Reset daily steps counter
     * This sets a new baseline for today's steps
     */
    fun resetDailySteps() {
        hasInitialCount = false
        initialStepCount = _state.value.totalSteps
        _state.value = _state.value.copy(
            dailySteps = 0,
            lastResetTimestamp = System.currentTimeMillis()
        )
        Log.d(TAG, "Daily steps reset. New baseline: $initialStepCount")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0].toInt()

            // On first reading, set the initial count
            if (!hasInitialCount) {
                initialStepCount = totalSteps
                hasInitialCount = true
                Log.d(TAG, "Initial step count set: $initialStepCount")
            }

            // Calculate daily steps (steps since app started or last reset)
            val dailySteps = (totalSteps - initialStepCount).coerceAtLeast(0)

            _state.value = _state.value.copy(
                totalSteps = totalSteps,
                dailySteps = dailySteps
            )

            Log.d(TAG, "Steps updated - Total: $totalSteps, Daily: $dailySteps")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Sensor accuracy changed: $accuracy")
    }

    /**
     * Start mock mode for testing on emulators
     * Simulates step counting by incrementing steps periodically
     */
    private fun startMockMode() {
        mockStepJob = viewModelScope.launch {
            while (true) {
                delay(MOCK_STEP_INTERVAL_MS)
                mockStepCount += MOCK_STEP_INCREMENT

                _state.value = _state.value.copy(
                    totalSteps = mockStepCount,
                    dailySteps = mockStepCount,
                    isListening = true // Show as "active" in mock mode
                )

                Log.d(TAG, "MOCK: Simulated steps: $mockStepCount (+$MOCK_STEP_INCREMENT)")
            }
        }
    }

    /**
     * Stop mock mode
     */
    private fun stopMockMode() {
        mockStepJob?.cancel()
        mockStepJob = null
        Log.d(TAG, "MOCK: Stopped")
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
        stopMockMode()
        Log.d(TAG, "ViewModel cleared, sensor listener unregistered")
    }
}


package com.example.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.sqrt

object SeismicSensorManager : SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    private val _vibrationForce = MutableStateFlow(0f)
    val vibrationForce: StateFlow<Float> = _vibrationForce

    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var isInitialized = false

    fun startListening(context: Context) {
        if (isInitialized) return
        try {
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            accelerometer?.let {
                sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
                LogManager.log("INFO", "Seismógrafo de Acelerómetro Físico inicializado con éxito.")
            } ?: run {
                LogManager.log("WARN", "Acelerómetro de hardware no disponible en este dispositivo.")
            }
            isInitialized = true
        } catch (e: Exception) {
            LogManager.log("ERROR", "Fallo al registrar Sensor de Acelerómetro Sísmico: ${e.message}")
        }
    }

    fun stopListening() {
        if (!isInitialized) return
        try {
            sensorManager?.unregisterListener(this)
            isInitialized = false
            LogManager.log("INFO", "Seismógrafo de Acelerómetro Físico detenido.")
        } catch (e: Exception) {
            // ignore
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
        
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        if (lastX != 0f || lastY != 0f || lastZ != 0f) {
            val deltaX = Math.abs(x - lastX)
            val deltaY = Math.abs(y - lastY)
            val deltaZ = Math.abs(z - lastZ)

            // Combine axes into a vibration force magnitude (excluding constant gravity shift changes)
            val force = sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()).toFloat()
            // Apply low-pass filter to smooth spikes and prevent jitter
            _vibrationForce.value = (_vibrationForce.value * 0.7f) + (force * 0.3f)
        }

        lastX = x
        lastY = y
        lastZ = z
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not required for vibration calculations
    }
}

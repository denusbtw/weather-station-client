package com.example.weatherstationclient.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * Менеджер апаратного датчика атмосферного тиску (Sensor.TYPE_PRESSURE).
 * Інкапсулює взаємодію з Android Sensor API та життєвий цикл прослуховування сенсора (SRP).
 */
class PressureSensorManager(context: Context) : SensorEventListener {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val pressureSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

    /**
     * Показує, чи присутній фізичний або емульований датчик барометра на пристрої.
     */
    val isSensorAvailable: Boolean
        get() = pressureSensor != null

    private var onPressureChangedListener: ((Float) -> Unit)? = null

    /**
     * Реєструє слухача сенсора з частотою оновлення для UI (SensorManager.SENSOR_DELAY_UI).
     */
    fun startListening(onPressureChanged: (Float) -> Unit) {
        onPressureChangedListener = onPressureChanged
        pressureSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    /**
     * Відписується від отримання оновлень сенсора для збереження енергії акумулятора.
     */
    fun stopListening() {
        sensorManager.unregisterListener(this)
        onPressureChangedListener = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_PRESSURE && event.values.isNotEmpty()) {
            val pressureValue = event.values[0]
            onPressureChangedListener?.invoke(pressureValue)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // У базових завданнях обробка калібрування точності не вимагається
    }
}

package com.example.weatherstationclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.weatherstationclient.sensor.PressureSensorManager
import com.example.weatherstationclient.ui.screens.WeatherStationScreen
import com.example.weatherstationclient.ui.theme.WeatherStationClientTheme

/**
 * Головна точка входу додатку (Activity).
 * Відповідає виключно за життєвий цикл та зв'язування компонентів (SRP).
 */
class MainActivity : ComponentActivity() {

    private lateinit var sensorManager: PressureSensorManager
    private var currentPressure by mutableFloatStateOf(1013.25f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ініціалізація виділеного менеджера сенсорів
        sensorManager = PressureSensorManager(this)

        setContent {
            WeatherStationClientTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeatherStationScreen(
                        pressure = currentPressure,
                        isSensorAvailable = sensorManager.isSensorAvailable,
                        onManualPressureChange = { simulatedValue ->
                            currentPressure = simulatedValue
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    /**
     * Життєвий цикл: відновлення зчитування показників сенсора при активному екрані.
     */
    override fun onResume() {
        super.onResume()
        sensorManager.startListening { pressureValue ->
            currentPressure = pressureValue
        }
    }

    /**
     * Життєвий цикл: зупинка опитування сенсора для збереження заряду батареї.
     */
    override fun onPause() {
        super.onPause()
        sensorManager.stopListening()
    }
}
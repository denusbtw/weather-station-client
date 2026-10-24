package com.example.weatherstationclient

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.weatherstationclient.cloud.FirebaseSensorSync
import com.example.weatherstationclient.ble.BleDevice
import com.example.weatherstationclient.ble.MockBleConnector
import com.example.weatherstationclient.ble.MockBleScanner
import com.example.weatherstationclient.sensor.PressureSensorManager
import com.example.weatherstationclient.ui.screens.WeatherStationScreen
import com.example.weatherstationclient.ui.theme.WeatherStationClientTheme
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Головна точка входу додатку (Activity).
 * Відповідає виключно за життєвий цикл та зв'язування компонентів (SRP).
 */
class MainActivity : ComponentActivity() {

    private lateinit var sensorManager: PressureSensorManager
    private lateinit var bleConnector: MockBleConnector
    private val cloudSync by lazy { (application as WeatherStationApplication).cloudSync }
    private val bleScanner = MockBleScanner()
    private val isScanning = MutableStateFlow(false)
    private val foundDevices = MutableStateFlow<List<BleDevice>>(emptyList())
    private var scanJob: Job? = null
    private var currentPressure by mutableFloatStateOf(1013.25f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ініціалізація виділеного менеджера сенсорів
        sensorManager = PressureSensorManager(this)
        bleConnector = MockBleConnector(lifecycleScope)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (isActive) {
                    delay(FirebaseSensorSync.SYNC_INTERVAL_MS)
                    cloudSync.sendPressure(currentPressure)
                }
            }
        }

        setContent {
            val scanning by isScanning.collectAsState()
            val devices by foundDevices.collectAsState()
            val connectionState by bleConnector.connectionState.collectAsState()
            val bleLog by bleConnector.logEntries.collectAsState()
            val cloudState by cloudSync.state.collectAsState()

            LaunchedEffect(cloudState.isInternetAvailable) {
                if (!cloudState.isInternetAvailable) {
                    Toast.makeText(
                        this@MainActivity,
                        "Немає інтернету. Дані очікують синхронізації (Pending).",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            WeatherStationClientTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeatherStationScreen(
                        pressure = currentPressure,
                        isSensorAvailable = sensorManager.isSensorAvailable,
                        onManualPressureChange = { simulatedValue ->
                            currentPressure = simulatedValue
                        },
                        isScanning = scanning,
                        foundDevices = devices,
                        connectionState = connectionState,
                        bleLog = bleLog,
                        cloudSyncState = cloudState,
                        onScan = ::scanForDevices,
                        onConnect = bleConnector::connect,
                        onDisconnect = bleConnector::disconnect,
                        onSendPressure = {
                            val payload = String.format(Locale.US, "PRESSURE_HPA=%.1f", currentPressure)
                            bleConnector.sendData(payload)
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
        scanJob?.cancel()
        isScanning.value = false
        bleConnector.disconnect()
    }

    private fun scanForDevices() {
        if (isScanning.value) return
        scanJob = lifecycleScope.launch {
            isScanning.value = true
            foundDevices.value = emptyList()
            try {
                foundDevices.value = bleScanner.scan()
            } finally {
                isScanning.value = false
            }
        }
    }
}

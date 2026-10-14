package com.example.weatherstationclient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherstationclient.domain.WeatherCalculator
import com.example.weatherstationclient.domain.WeatherStatus
import com.example.weatherstationclient.ble.BleConnectionState
import com.example.weatherstationclient.ble.BleDevice
import com.example.weatherstationclient.ble.BleLogEntry
import com.example.weatherstationclient.ui.components.BarometerScaleCanvas
import com.example.weatherstationclient.ui.components.BleControlCard
import com.example.weatherstationclient.ui.components.ThresholdsLegend

/**
 * Головний екран моніторингу погодної станції (WeatherStationScreen).
 * Відповідає виключно за компонування та відображення даних на UI.
 */
@Composable
fun WeatherStationScreen(
    pressure: Float,
    isSensorAvailable: Boolean,
    onManualPressureChange: (Float) -> Unit,
    isScanning: Boolean,
    foundDevices: List<BleDevice>,
    connectionState: BleConnectionState,
    bleLog: List<BleLogEntry>,
    onScan: () -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onSendPressure: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val pressureMmHg = WeatherCalculator.hpaToMmHg(pressure)
    val weatherStatus = WeatherCalculator.calculateWeatherStatus(pressure)
    val progressFraction = WeatherCalculator.calculateProgressFraction(pressure)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Секція заголовку та статус датчика
        HeaderSection(isSensorAvailable = isSensorAvailable)

        BleControlCard(
            isScanning = isScanning,
            foundDevices = foundDevices,
            connectionState = connectionState,
            logEntries = bleLog,
            onScan = onScan,
            onConnect = onConnect,
            onDisconnect = onDisconnect,
            onSendPressure = onSendPressure
        )

        // КАРТКА 1: Показники тиску (гПа, мм рт. ст.) та розрахований стан погоди
        PressureMetricsCard(
            pressure = pressure,
            pressureMmHg = pressureMmHg,
            weatherStatus = weatherStatus
        )

        // КАРТКА 2: Графічна шкала на Canvas та LinearProgressIndicator
        ScaleVisualizationCard(
            pressure = pressure,
            progressFraction = progressFraction,
            statusColor = weatherStatus.color
        )

        // КАРТКА 3: Повзунок для тестування в емуляторі
        SimulationControlCard(
            pressure = pressure,
            onPressureChange = onManualPressureChange
        )
    }
}

@Composable
private fun HeaderSection(
    isSensorAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Weather Station Client",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "SensorManager інтегровано",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isSensorAvailable) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
        ) {
            Text(
                text = if (isSensorAvailable) "Сенсор онлайн" else "Емуляція тиску",
                color = if (isSensorAvailable) Color(0xFF2E7D32) else Color(0xFFEF6C00),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun PressureMetricsCard(
    pressure: Float,
    pressureMmHg: Float,
    weatherStatus: WeatherStatus,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Атмосферний тиск (Sensor.TYPE_PRESSURE)",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = String.format("%.1f", pressure),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = weatherStatus.color
                    )
                    Text(
                        text = "гПа (hPa)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format("%.1f", pressureMmHg),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "мм рт. ст. (mmHg)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = weatherStatus.color.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(weatherStatus.color, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = weatherStatus.title,
                            fontWeight = FontWeight.Bold,
                            color = weatherStatus.color,
                            fontSize = 15.sp
                        )
                        Text(
                            text = weatherStatus.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScaleVisualizationCard(
    pressure: Float,
    progressFraction: Float,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Графічний індикатор шкали тиску (Canvas)",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            BarometerScaleCanvas(
                currentPressure = pressure,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Графічний індикатор заповнення (ProgressBar):",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = statusColor,
                trackColor = Color.LightGray.copy(alpha = 0.35f)
            )

            Spacer(modifier = Modifier.height(20.dp))
            ThresholdsLegend()
        }
    }
}

@Composable
private fun SimulationControlCard(
    pressure: Float,
    onPressureChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Імітація зміни тиску (для перевірки в емуляторі)",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = pressure,
                onValueChange = onPressureChange,
                valueRange = 960f..1060f,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Значення автоматично оновлюються як із віртуального сенсора, так і через повзунок",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

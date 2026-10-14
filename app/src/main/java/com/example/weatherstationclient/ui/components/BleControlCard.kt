package com.example.weatherstationclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherstationclient.ble.BleConnectionState
import com.example.weatherstationclient.ble.BleDevice
import com.example.weatherstationclient.ble.BleDeviceProfile
import com.example.weatherstationclient.ble.BleLogEntry

@Composable
fun BleControlCard(
    isScanning: Boolean,
    foundDevices: List<BleDevice>,
    connectionState: BleConnectionState,
    logEntries: List<BleLogEntry>,
    onScan: () -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onSendPressure: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Віртуальний BLE-хаб", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(
                "Mock-з’єднання для передавання показників барометра",
                style = MaterialTheme.typography.bodySmall
            )

            val statusText = when (connectionState) {
                BleConnectionState.DISCONNECTED -> "Disconnected"
                BleConnectionState.CONNECTING -> "Connecting…"
                BleConnectionState.CONNECTED -> "Connected"
            }
            val statusColor = when (connectionState) {
                BleConnectionState.DISCONNECTED -> Color(0xFFD32F2F)
                BleConnectionState.CONNECTING -> Color(0xFFF57C00)
                BleConnectionState.CONNECTED -> Color(0xFF388E3C)
            }
            Text("Статус: $statusText", color = statusColor, fontWeight = FontWeight.Bold)

            Button(onClick = onScan, enabled = !isScanning) {
                Text("Пошук пристроїв")
            }
            if (isScanning) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text("Сканування…", style = MaterialTheme.typography.bodySmall)
            }

            if (foundDevices.isEmpty() && !isScanning) {
                Text("Натисніть «Пошук пристроїв», щоб знайти віртуальний хаб.")
            }
            foundDevices.forEach { device ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(device.name, fontWeight = FontWeight.SemiBold)
                    Text(device.address, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    if (connectionState == BleConnectionState.DISCONNECTED) {
                        OutlinedButton(onClick = onConnect) { Text("Підключитися") }
                    } else {
                        OutlinedButton(onClick = onDisconnect) { Text("Відключитися") }
                    }
                }
            }

            Button(
                onClick = onSendPressure,
                enabled = connectionState == BleConnectionState.CONNECTED
            ) {
                Text("Надіслати поточний тиск")
            }

            Text("GATT-профіль", fontWeight = FontWeight.SemiBold)
            Text("Service: ${BleDeviceProfile.SERVICE_UUID}", fontSize = 10.sp)
            Text("Status (Read/Notify): ${BleDeviceProfile.STATUS_UUID}", fontSize = 10.sp)
            Text("Data (Write): ${BleDeviceProfile.DATA_UUID}", fontSize = 10.sp)

            Text("Термінал обміну", fontWeight = FontWeight.SemiBold)
            SelectionContainer {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF14202B), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (logEntries.isEmpty()) {
                        Text("Очікування з’єднання…", color = Color(0xFFB6C8D4), fontSize = 11.sp)
                    }
                    logEntries.forEach { entry ->
                        Text(
                            "${entry.time}  ${entry.message}",
                            color = Color(0xFFA8E6B1),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

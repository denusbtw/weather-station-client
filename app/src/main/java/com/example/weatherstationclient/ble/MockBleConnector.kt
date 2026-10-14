package com.example.weatherstationclient.ble

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Імітація GATT Read, Write і Notify для погодного хаба. */
class MockBleConnector(private val scope: CoroutineScope) : IBleConnector {
    private val _connectionState = MutableStateFlow(BleConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<BleConnectionState> = _connectionState

    private val _logEntries = MutableStateFlow<List<BleLogEntry>>(emptyList())
    override val logEntries: StateFlow<List<BleLogEntry>> = _logEntries

    private var connectionJob: Job? = null
    private var connectionEpoch = 0

    override fun connect() {
        if (_connectionState.value != BleConnectionState.DISCONNECTED) return
        _connectionState.value = BleConnectionState.CONNECTING
        appendLog("CONNECT → ${BleDeviceProfile.DEVICE_NAME}")
        connectionJob = scope.launch {
            delay(500)
            _connectionState.value = BleConnectionState.CONNECTED
            appendLog("CONNECTED • сервіс ${BleDeviceProfile.SERVICE_UUID}")
            appendLog("READ ← статус [${BleDeviceProfile.STATUS_UUID}]: READY")
        }
    }

    override fun disconnect() {
        connectionJob?.cancel()
        connectionEpoch++
        if (_connectionState.value == BleConnectionState.DISCONNECTED) return
        _connectionState.value = BleConnectionState.DISCONNECTED
        appendLog("DISCONNECT → ${BleDeviceProfile.DEVICE_NAME}")
    }

    override fun sendData(data: String) {
        if (_connectionState.value != BleConnectionState.CONNECTED) {
            appendLog("WRITE відхилено: немає з’єднання")
            return
        }
        if (data.isBlank()) return
        appendLog("WRITE → дані [${BleDeviceProfile.DATA_UUID}]: $data")
        val sentDuringEpoch = connectionEpoch
        scope.launch {
            delay(350)
            if (_connectionState.value == BleConnectionState.CONNECTED && sentDuringEpoch == connectionEpoch) {
                appendLog("NOTIFY ← статус [${BleDeviceProfile.STATUS_UUID}]: RECEIVED")
            }
        }
    }

    private fun appendLog(message: String) {
        Log.i("MockBleConnector", message)
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _logEntries.value = (_logEntries.value + BleLogEntry(time, message)).takeLast(50)
    }
}

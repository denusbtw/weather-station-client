package com.example.weatherstationclient.ble

import kotlinx.coroutines.flow.StateFlow

enum class BleConnectionState { DISCONNECTED, CONNECTING, CONNECTED }

data class BleLogEntry(val time: String, val message: String)

/** Контракт зв'язку: реалізацію mock можна згодом замінити реальним BLE-клієнтом. */
interface IBleConnector {
    val connectionState: StateFlow<BleConnectionState>
    val logEntries: StateFlow<List<BleLogEntry>>

    fun connect()
    fun disconnect()
    fun sendData(data: String)
}

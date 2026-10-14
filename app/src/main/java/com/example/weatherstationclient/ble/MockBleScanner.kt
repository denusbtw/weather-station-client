package com.example.weatherstationclient.ble

import kotlinx.coroutines.delay

/** Імітує пошук пристроїв без звернення до Bluetooth-адаптера. */
class MockBleScanner {
    suspend fun scan(): List<BleDevice> {
        delay(2_000)
        return listOf(BleDevice())
    }
}

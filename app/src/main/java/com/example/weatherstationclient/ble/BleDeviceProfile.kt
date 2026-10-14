package com.example.weatherstationclient.ble

import java.util.UUID

/** Віртуальний GATT-профіль погодного хаба для лабораторної роботи №2. */
object BleDeviceProfile {
    const val DEVICE_NAME = "Weather Station Hub"
    const val DEVICE_ADDRESS = "MOCK:57:41:54:48:52"

    val SERVICE_UUID: UUID = UUID.fromString("7f0b1000-65e8-4b26-a9f0-9d6417a90201")
    val STATUS_UUID: UUID = UUID.fromString("7f0b1001-65e8-4b26-a9f0-9d6417a90201") // Read / Notify
    val DATA_UUID: UUID = UUID.fromString("7f0b1002-65e8-4b26-a9f0-9d6417a90201") // Write
}

data class BleDevice(
    val name: String = BleDeviceProfile.DEVICE_NAME,
    val address: String = BleDeviceProfile.DEVICE_ADDRESS
)

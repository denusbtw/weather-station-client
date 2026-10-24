package com.example.weatherstationclient.cloud

data class SensorData(
    val deviceId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val value: Float,
    val type: String = "pressure",
    val unit: String = "hPa"
) {
    // Explicit JSON field names also work when the release build obfuscates classes.
    fun toMap(): Map<String, Any> = mapOf(
        "deviceId" to deviceId,
        "timestamp" to timestamp,
        "value" to value,
        "type" to type,
        "unit" to unit
    )
}

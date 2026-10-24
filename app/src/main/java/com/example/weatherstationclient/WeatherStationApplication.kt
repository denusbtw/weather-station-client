package com.example.weatherstationclient

import android.app.Application
import com.example.weatherstationclient.cloud.FirebaseSensorSync

class WeatherStationApplication : Application() {
    // One queue and connection observer per process, including after Activity recreation.
    val cloudSync: FirebaseSensorSync by lazy { FirebaseSensorSync(this) }
}

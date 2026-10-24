package com.example.weatherstationclient.cloud

import android.content.Context
import com.google.gson.Gson
import java.io.IOException

class PreferencesSensorStore(context: Context) : PendingSensorStore {
    private val preferences = context.getSharedPreferences("sensor_http_outbox", Context.MODE_PRIVATE)
    private val gson = Gson()

    override fun load(): List<PendingSensorRecord> {
        val json = preferences.getString("records", null) ?: return emptyList()
        return gson.fromJson(json, Array<PendingSensorRecord>::class.java).toList()
    }

    override fun save(records: List<PendingSensorRecord>) {
        // commit is intentional: an unacknowledged measurement must survive process death.
        if (!preferences.edit().putString("records", gson.toJson(records)).commit()) {
            throw IOException("Не вдалося зберегти чергу вимірювань на пристрої")
        }
    }
}

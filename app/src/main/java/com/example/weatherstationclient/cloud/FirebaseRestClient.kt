package com.example.weatherstationclient.cloud

import java.io.IOException
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Path

interface FirebaseSensorApi {
    @PUT("sensor_data/{deviceId}/{recordId}.json")
    suspend fun putReading(
        @Path("deviceId") deviceId: String,
        @Path("recordId") recordId: String,
        @Body payload: Map<String, @JvmSuppressWildcards Any>
    ): Response<Unit>
}

class FirebaseRestClient(databaseUrl: String) {
    private val api = Retrofit.Builder()
        .baseUrl(databaseUrl.trimEnd('/') + "/")
        .client(
            OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .callTimeout(30, TimeUnit.SECONDS)
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(FirebaseSensorApi::class.java)

    suspend fun upload(record: PendingSensorRecord): Int {
        val response = api.putReading(
            record.payload.deviceId,
            record.recordId,
            record.payload.toMap()
        )
        if (!response.isSuccessful) {
            val error = response.errorBody()?.use { it.string().take(200) }.orEmpty()
            throw IOException("HTTP ${response.code()}: $error")
        }
        return response.code()
    }
}

package com.example.weatherstationclient.cloud

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import com.google.firebase.FirebaseApp
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext

/** Application-scoped repository: durable outbox + observable HTTPS writes to Firebase. */
class FirebaseSensorSync(context: Context) {
    private val connectivity = context.getSystemService(ConnectivityManager::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val uploadMutex = Mutex()
    // Lazy initialization happens on Dispatchers.IO, including loading a restored outbox.
    private val outbox by lazy { SensorOutbox(PreferencesSensorStore(context)) }
    private val client = FirebaseRestClient(
        requireNotNull(FirebaseApp.getInstance().options.databaseUrl) {
            "У google-services.json відсутня адреса Realtime Database"
        }
    )
    private val preferences = context.getSharedPreferences("sensor_cloud", Context.MODE_PRIVATE)
    private val deviceId = preferences.getString("device_id", null)
        ?: UUID.randomUUID().toString().also {
            preferences.edit().putString("device_id", it).apply()
        }
    private val mutableState = MutableStateFlow(CloudSyncState(isInternetAvailable = hasInternet()))
    val state = mutableState.asStateFlow()

    init {
        connectivity.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = networkChanged()
            override fun onLost(network: Network) = networkChanged()
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                networkChanged()
            }
        })
        scope.launch {
            while (isActive) {
                flushPending()
                delay(SYNC_INTERVAL_MS)
            }
        }
    }

    suspend fun sendPressure(value: Float) = withContext(Dispatchers.IO) {
        if (!value.isFinite()) return@withContext
        try {
            val record = PendingSensorRecord(
                recordId = UUID.randomUUID().toString(),
                payload = SensorData(deviceId = deviceId, value = value)
            )
            outbox.enqueue(record)
            mutableState.update { it.copy(pendingCount = outbox.size) }
            scope.launch { flushPending() }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            recordFailure(error)
        }
    }

    private suspend fun flushPending() {
        if (!uploadMutex.tryLock()) return
        try {
            mutableState.update {
                it.copy(isInternetAvailable = hasInternet(), pendingCount = outbox.size)
            }
            while (hasInternet()) {
                val record = outbox.peek() ?: break
                mutableState.update { it.copy(isUploading = true) }
                val statusCode = client.upload(record)
                // Retrying the same PUT path after a lost response cannot duplicate a reading.
                outbox.acknowledge(record.recordId)
                mutableState.update {
                    it.copy(
                        pendingCount = outbox.size,
                        lastSuccessAt = System.currentTimeMillis(),
                        lastSyncedValue = record.payload.value,
                        lastHttpStatus = statusCode,
                        lastError = null
                    )
                }
                Log.i(TAG, "HTTP $statusCode PUT sensor_data/${record.payload.deviceId}/${record.recordId}.json: ${record.payload.toMap()}")
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            // Keep the original record and timestamp for the next network event or retry tick.
            recordFailure(error)
        } finally {
            mutableState.update { it.copy(isUploading = false) }
            uploadMutex.unlock()
        }
    }

    private fun recordFailure(error: Exception) {
        mutableState.update { it.copy(lastError = error.localizedMessage ?: "Помилка синхронізації") }
        Log.e(TAG, "HTTP sync failed; measurement remains Pending", error)
    }

    private fun networkChanged() {
        mutableState.update { it.copy(isInternetAvailable = hasInternet()) }
        scope.launch { flushPending() }
    }

    private fun hasInternet(): Boolean {
        val capabilities = connectivity.getNetworkCapabilities(connectivity.activeNetwork)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    companion object {
        const val SYNC_INTERVAL_MS = 5_000L
        private const val TAG = "FirebaseSensorSync"
    }
}

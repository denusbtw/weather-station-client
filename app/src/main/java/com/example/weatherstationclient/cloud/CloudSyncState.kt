package com.example.weatherstationclient.cloud

enum class CloudSyncStatus { WAITING, PENDING, SYNCING, SUCCESS, ERROR }

data class CloudSyncState(
    val isInternetAvailable: Boolean = false,
    val isUploading: Boolean = false,
    val pendingCount: Int = 0,
    val lastSuccessAt: Long? = null,
    val lastSyncedValue: Float? = null,
    val lastHttpStatus: Int? = null,
    val lastError: String? = null
) {
    val status: CloudSyncStatus
        get() = when {
            !isInternetAvailable -> CloudSyncStatus.PENDING
            lastError != null -> CloudSyncStatus.ERROR
            isUploading -> CloudSyncStatus.SYNCING
            pendingCount > 0 -> CloudSyncStatus.PENDING
            lastSuccessAt != null -> CloudSyncStatus.SUCCESS
            else -> CloudSyncStatus.WAITING
        }
}

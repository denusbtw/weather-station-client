package com.example.weatherstationclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.weatherstationclient.cloud.CloudSyncState
import com.example.weatherstationclient.cloud.CloudSyncStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CloudSyncCard(state: CloudSyncState, modifier: Modifier = Modifier) {
    val status = state.status
    val color = when (status) {
        CloudSyncStatus.SUCCESS -> Color(0xFF2E7D32)
        CloudSyncStatus.ERROR, CloudSyncStatus.PENDING -> Color(0xFFC62828)
        else -> Color(0xFFEF6C00)
    }
    val message = when (status) {
        CloudSyncStatus.WAITING -> "Очікування першого вимірювання"
        CloudSyncStatus.PENDING -> if (state.isInternetAvailable) {
            "Pending — очікування повторного надсилання"
        } else {
            "Pending — немає інтернету"
        }
        CloudSyncStatus.SYNCING -> "Надсилання даних…"
        CloudSyncStatus.SUCCESS -> "Дані збережено у Firebase"
        CloudSyncStatus.ERROR -> "Помилка синхронізації"
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(Modifier.size(12.dp).background(color, CircleShape))
                Text("Хмарна синхронізація", fontWeight = FontWeight.SemiBold)
            }
            Text(message, color = color)
            Text(
                "Атмосферний тиск • кожні 5 секунд",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "У черзі: ${state.pendingCount}" +
                    (state.lastHttpStatus?.let { " • останній успішний: HTTP $it" } ?: ""),
                style = MaterialTheme.typography.bodySmall
            )
            state.lastSuccessAt?.let { timestamp ->
                val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
                Text(
                    "Останній запис: $time • ${String.format(Locale.getDefault(), "%.1f", state.lastSyncedValue)} гПа",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            state.lastError?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

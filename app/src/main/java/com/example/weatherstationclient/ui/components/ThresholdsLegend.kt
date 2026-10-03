package com.example.weatherstationclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Легенда порогових значень та погодних інтерпретацій для шкали тиску.
 */
@Composable
fun ThresholdsLegend(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Порогові діапазони:",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        LegendItem(
            color = Color(0xFFD32F2F),
            range = "< 995 гПа",
            name = "Штормове попередження (Циклон)"
        )
        LegendItem(
            color = Color(0xFF1976D2),
            range = "995 – 1010 гПа",
            name = "Знижений тиск (Опади)"
        )
        LegendItem(
            color = Color(0xFF388E3C),
            range = "1010 – 1022 гПа",
            name = "Нормальний тиск (Помірно)"
        )
        LegendItem(
            color = Color(0xFFF57C00),
            range = "1022 – 1035 гПа",
            name = "Підвищений тиск (Ясно)"
        )
        LegendItem(
            color = Color(0xFF7B1FA2),
            range = "> 1035 гПа",
            name = "Потужний антициклон"
        )
    }
}

@Composable
fun LegendItem(
    color: Color,
    range: String,
    name: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = range,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(90.dp)
        )
        Text(
            text = name,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

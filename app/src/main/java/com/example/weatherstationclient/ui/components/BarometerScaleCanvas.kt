package com.example.weatherstationclient.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.weatherstationclient.domain.WeatherCalculator

/**
 * Кастомний графічний індикатор шкали атмосферного тиску на Canvas.
 * Відображає кольорові зони погодних діапазонів та стрілку-вказівник поточного значення.
 */
@Composable
fun BarometerScaleCanvas(
    currentPressure: Float,
    modifier: Modifier = Modifier
) {
    val minP = WeatherCalculator.MIN_PRESSURE
    val maxP = WeatherCalculator.MAX_PRESSURE

    Canvas(modifier = modifier) {
        val totalWidth = size.width
        val barHeight = size.height * 0.45f
        val barTop = 4.dp.toPx()

        fun pToX(p: Float): Float {
            val clamped = p.coerceIn(minP, maxP)
            return ((clamped - minP) / (maxP - minP)) * totalWidth
        }

        val x0 = 0f
        val xStorm = pToX(995f)
        val xRain = pToX(1010f)
        val xNormal = pToX(1022f)
        val xClear = pToX(1035f)
        val xEnd = totalWidth

        // Зона 1: Штормове попередження (< 995)
        drawRoundRect(
            color = Color(0xFFD32F2F),
            topLeft = Offset(x0, barTop),
            size = Size(xStorm - x0, barHeight),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
        // Зона 2: Знижений тиск (995 - 1010)
        drawRect(
            color = Color(0xFF1976D2),
            topLeft = Offset(xStorm, barTop),
            size = Size(xRain - xStorm, barHeight)
        )
        // Зона 3: Нормальний тиск (1010 - 1022)
        drawRect(
            color = Color(0xFF388E3C),
            topLeft = Offset(xRain, barTop),
            size = Size(xNormal - xRain, barHeight)
        )
        // Зона 4: Підвищений тиск (1022 - 1035)
        drawRect(
            color = Color(0xFFF57C00),
            topLeft = Offset(xNormal, barTop),
            size = Size(xClear - xNormal, barHeight)
        )
        // Зона 5: Потужний антициклон (> 1035)
        drawRoundRect(
            color = Color(0xFF7B1FA2),
            topLeft = Offset(xClear, barTop),
            size = Size(xEnd - xClear, barHeight),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )

        // Вказівник поточного тиску (стрілка + маркер)
        val pointerX = pToX(currentPressure)
        drawCircle(
            color = Color.Black,
            radius = 7.dp.toPx(),
            center = Offset(pointerX, barTop + barHeight / 2)
        )
        drawCircle(
            color = Color.White,
            radius = 4.dp.toPx(),
            center = Offset(pointerX, barTop + barHeight / 2)
        )
        drawLine(
            color = Color.Black,
            start = Offset(pointerX, barTop + barHeight),
            end = Offset(pointerX, size.height),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

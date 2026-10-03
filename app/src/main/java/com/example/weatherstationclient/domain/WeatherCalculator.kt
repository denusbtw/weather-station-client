package com.example.weatherstationclient.domain

import androidx.compose.ui.graphics.Color

/**
 * Модель статусу погодних умов за результатами вимірювання атмосферного тиску.
 */
data class WeatherStatus(
    val title: String,
    val color: Color,
    val description: String
)

/**
 * Калькулятор та бізнес-правила розрахунку показників тиску і погодних умов.
 * Відповідає виключно за математичні та логічні перетворення даних (Single Responsibility).
 */
object WeatherCalculator {

    const val MIN_PRESSURE = 960f
    const val MAX_PRESSURE = 1060f
    private const val HPA_TO_MMHG = 0.750062f

    /**
     * Конвертація гектопаскалів (гПа / hPa) у міліметри ртутного стовпчика (мм рт. ст. / mmHg).
     */
    fun hpaToMmHg(pressureHpa: Float): Float = pressureHpa * HPA_TO_MMHG

    /**
     * Розрахунок коефіцієнта заповнення для індикатора шкали (від 0.0 до 1.0).
     */
    fun calculateProgressFraction(pressureHpa: Float): Float {
        return ((pressureHpa - MIN_PRESSURE) / (MAX_PRESSURE - MIN_PRESSURE)).coerceIn(0f, 1f)
    }

    /**
     * Визначення словесного стану погоди та колірного індикатора на основі порогових діапазонів (Thresholds).
     */
    fun calculateWeatherStatus(pressureHpa: Float): WeatherStatus {
        return when {
            pressureHpa < 995f -> WeatherStatus(
                title = "Штормове попередження",
                color = Color(0xFFD32F2F),
                description = "Глибокий циклон: стрімке падіння тиску, шквалистий вітер та зливи"
            )
            pressureHpa < 1010f -> WeatherStatus(
                title = "Знижений тиск",
                color = Color(0xFF1976D2),
                description = "Циклонічний вплив: хмарна погода, висока ймовірність затяжних опадів"
            )
            pressureHpa <= 1022f -> WeatherStatus(
                title = "Нормальний тиск",
                color = Color(0xFF388E3C),
                description = "Сприятливі погодні умови, помірна хмарність без значних опадів"
            )
            pressureHpa <= 1035f -> WeatherStatus(
                title = "Підвищений тиск",
                color = Color(0xFFF57C00),
                description = "Антициклон: малохмарна, суха та сонячна погода"
            )
            else -> WeatherStatus(
                title = "Потужний антициклон",
                color = Color(0xFF7B1FA2),
                description = "Аномально високий тиск: сухе повітря, тривала відсутність опадів"
            )
        }
    }
}

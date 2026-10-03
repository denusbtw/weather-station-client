package com.example.weatherstationclient

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherstationclient.ui.theme.WeatherStationClientTheme

/**
 * ЕТАП №3: Налаштування SensorManager, SensorEventListener та життєвого циклу.
 */
class MainActivity : ComponentActivity(), SensorEventListener {

    // 1. Оголошення SensorManager та сенсора барометра
    private lateinit var sensorManager: SensorManager
    private var pressureSensor: Sensor? = null

    // 2. Стан тиску для реактивного оновлення UI при надходженні даних з onSensorChanged
    private var currentPressure by mutableFloatStateOf(1013.25f)
    private var isSensorAvailable by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 3. Ініціалізація SensorManager через системний сервіс
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // 4. Отримання сенсора тиску (TYPE_PRESSURE)
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        isSensorAvailable = (pressureSensor != null)

        setContent {
            WeatherStationClientTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeatherStationScreen(
                        pressure = currentPressure,
                        isSensorAvailable = isSensorAvailable,
                        onManualPressureChange = { simulatedValue ->
                            // Дозволяє змінювати значення вручну, якщо в емуляторі відсутній барометр
                            currentPressure = simulatedValue
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    /**
     * Життєвий цикл: реєстрація слухача у методі onResume().
     * Коли додаток виходить на передній план, починаємо отримувати дані із сенсора.
     */
    override fun onResume() {
        super.onResume()
        pressureSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    /**
     * Життєвий цикл: зупинка зчитування у методі onPause().
     * Обов'язкова вимога методички: викликаємо unregisterListener(),
     * щоб сенсор не споживав заряд акумулятора у фоновому режимі.
     */
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    /**
     * Обробник подій сенсора: викликається автоматично при зміні атмосферного тиску.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        if (event.sensor.type == Sensor.TYPE_PRESSURE) {
            // event.values[0] повертає значення атмосферного тиску в гПа (hPa / mbar)
            currentPressure = event.values[0]
        }
    }

    /**
     * Викликається при зміні точності вимірювань сенсора.
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // У базових лабораторних роботах обробка калібрування не вимагається
    }
}

@Composable
fun WeatherStationScreen(
    pressure: Float,
    isSensorAvailable: Boolean,
    onManualPressureChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val pressureMmHg = pressure * 0.750062f

    // Визначення погодного стану на основі порогових значень (Thresholds)
    val (weatherTitle, statusColor, weatherDescription) = when {
        pressure < 995f -> Triple(
            "Штормове попередження",
            Color(0xFFD32F2F),
            "Глибокий циклон: стрімке падіння тиску, шквалистий вітер та зливи"
        )
        pressure < 1010f -> Triple(
            "Знижений тиск",
            Color(0xFF1976D2),
            "Циклонічний вплив: хмарна погода, висока ймовірність затяжних опадів"
        )
        pressure <= 1022f -> Triple(
            "Нормальний тиск",
            Color(0xFF388E3C),
            "Сприятливі погодні умови, помірна хмарність без значних опадів"
        )
        pressure <= 1035f -> Triple(
            "Підвищений тиск",
            Color(0xFFF57C00),
            "Антициклон: малохмарна, суха та сонячна погода"
        )
        else -> Triple(
            "Потужний антициклон",
            Color(0xFF7B1FA2),
            "Аномально високий тиск: сухе повітря, тривала відсутність опадів"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Заголовок та статус підключення сенсора
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Weather Station Client",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Етап №3: SensorManager інтегровано",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isSensorAvailable) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
            ) {
                Text(
                    text = if (isSensorAvailable) "Сенсор онлайн" else "Емуляція тиску",
                    color = if (isSensorAvailable) Color(0xFF2E7D32) else Color(0xFFEF6C00),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // КАРТКА 1: ПОКАЗНИКИ ТИСКУ ТА ПОТОЧНИЙ СТАН
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Атмосферний тиск (Sensor.TYPE_PRESSURE)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = String.format("%.1f", pressure),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusColor
                        )
                        Text(
                            text = "гПа (hPa)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = String.format("%.1f", pressureMmHg),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "мм рт. ст. (mmHg)",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusColor.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(statusColor, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = weatherTitle,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                fontSize = 15.sp
                            )
                            Text(
                                text = weatherDescription,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // КАРТКА 2: ГРАФІЧНИЙ ІНДИКАТОР НА CANVAS ТА PROGRESSBAR
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Графічний індикатор шкали тиску (Canvas)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                BarometerScaleCanvas(
                    currentPressure = pressure,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                val progressFraction = ((pressure - 960f) / (1060f - 960f)).coerceIn(0f, 1f)
                Text(
                    text = "Графічний індикатор заповнення (ProgressBar):",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                    color = statusColor,
                    trackColor = Color.LightGray.copy(alpha = 0.35f)
                )

                Spacer(modifier = Modifier.height(20.dp))
                ThresholdsLegend()
            }
        }

        // КАРТКА 3: ТЕСТОВИЙ РЕЖИМ СИМУЛЯЦІЇ ТИСКУ
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Імітація зміни тиску (для перевірки в емуляторі)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = pressure,
                    onValueChange = onManualPressureChange,
                    valueRange = 960f..1060f,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Значення автоматично оновлюються як із віртуального сенсора, так і через повзунок",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BarometerScaleCanvas(
    currentPressure: Float,
    modifier: Modifier = Modifier
) {
    val minP = 960f
    val maxP = 1060f

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

        drawRoundRect(
            color = Color(0xFFD32F2F),
            topLeft = Offset(x0, barTop),
            size = Size(xStorm - x0, barHeight),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
        drawRect(
            color = Color(0xFF1976D2),
            topLeft = Offset(xStorm, barTop),
            size = Size(xRain - xStorm, barHeight)
        )
        drawRect(
            color = Color(0xFF388E3C),
            topLeft = Offset(xRain, barTop),
            size = Size(xNormal - xRain, barHeight)
        )
        drawRect(
            color = Color(0xFFF57C00),
            topLeft = Offset(xNormal, barTop),
            size = Size(xClear - xNormal, barHeight)
        )
        drawRoundRect(
            color = Color(0xFF7B1FA2),
            topLeft = Offset(xClear, barTop),
            size = Size(xEnd - xClear, barHeight),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )

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

@Composable
fun ThresholdsLegend() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = "Порогові діапазони:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        LegendItem(color = Color(0xFFD32F2F), range = "< 995 гПа", name = "Штормове попередження (Циклон)")
        LegendItem(color = Color(0xFF1976D2), range = "995 – 1010 гПа", name = "Знижений тиск (Опади)")
        LegendItem(color = Color(0xFF388E3C), range = "1010 – 1022 гПа", name = "Нормальний тиск (Помірно)")
        LegendItem(color = Color(0xFFF57C00), range = "1022 – 1035 гПа", name = "Підвищений тиск (Ясно)")
        LegendItem(color = Color(0xFF7B1FA2), range = "> 1035 гПа", name = "Потужний антициклон")
    }
}

@Composable
fun LegendItem(color: Color, range: String, name: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = range, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(90.dp))
        Text(text = name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
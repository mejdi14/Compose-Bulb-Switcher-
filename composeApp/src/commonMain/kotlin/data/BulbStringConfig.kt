package data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class BulbStringConfig(
    val initialTouchPosition: Offset,
    val bulbCenterX: Float,
    val initialYOffset: Float,
    val waveSequence: List<Pair<Float, Int>>,
    val lengthSequence: List<Float>,
    val touchThreshold: Float = 50f,
    val strokeWidth: Float = 3f,
    val circleRadius: Float = 5f,
    val lineColor: Color = Color.Black,
    val circleColor: Color = Color.Black
)
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.dp

import composablelightbulb.composeapp.generated.resources.Res
import composablelightbulb.composeapp.generated.resources.bulb_off
import composablelightbulb.composeapp.generated.resources.bulb_on
import composablelightbulb.composeapp.generated.resources.bulb_output
import composablelightbulb.composeapp.generated.resources.bulb_switcher
import composablelightbulb.composeapp.generated.resources.compose_multiplatform
import composablelightbulb.composeapp.generated.resources.light_bulb
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@Composable
@Preview
fun App() {
    BulbSwitcher()
}

@Composable
private fun BulbSwitcher() {
    var touchPosition by remember { mutableStateOf(Offset(100f, 100f)) }
    var isTouching by remember { mutableStateOf(false) }
    val bulbCenterX = remember { 100f }
    val endPoint = remember { mutableStateOf(Offset(bulbCenterX, 100f)) }
    val waveAmplitude = remember { Animatable(0f) }  // Controls the amplitude of the wave
    val yOffset = remember { Animatable(100f) }  // Controls the vertical offset of the bottom tip
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(isTouching) {
        if (!isTouching) {
            val waveSequence = sequenceOf(
                60f to 1,   // First wave amplitude and direction (right)
                40f to -1,  // Second wave amplitude and direction (left)
                20f to 1,   // Third wave amplitude and direction (right)
                10f to -1
            )
            val lengthSequence = sequenceOf(
                60f,
                100f,
                80f,
                100f,
                80f,
                100f,
            )


            coroutineScope.launch {
                val lengthAnimations = launch {
                    lengthSequence.forEach { length ->
                        yOffset.animateTo(
                            targetValue = length,
                            animationSpec = tween(durationMillis = 100, easing = LinearEasing)
                        )
                    }
                }
                val waveAnimations = launch {
                    waveSequence.forEach { (amplitude, direction) ->
                        waveAmplitude.animateTo(
                            targetValue = amplitude * direction,  // Positive for right, negative for left
                            animationSpec = tween(durationMillis = 100, easing = LinearEasing)
                        )
                        waveAmplitude.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(durationMillis = 100, easing = LinearEasing)
                        )
                    }
                }
                lengthAnimations.join()
                waveAnimations.join()

                waveAmplitude.snapTo(0f)
            }
        }
    }

    MaterialTheme
        Column(modifier = Modifier.fillMaxSize().background(color = Color(0xFF29363e)),
            horizontalAlignment = Alignment.End) {
            Spacer(Modifier.height(20.dp))
            Image(
                painterResource(Res.drawable.bulb_switcher),
                contentDescription = null,
                modifier = Modifier.graphicsLayer {
                    translationY = 18f
                }
            )
            Canvas(modifier = Modifier.weight(1f).size(width = 100.dp, height = 50.dp)
                .pointerInput(Unit) {
                    forEachGesture {
                        awaitPointerEventScope {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            // Check if the touch down is close enough to the endpoint of the string.
                            if (abs(down.position.x - endPoint.value.x) <= 50f && abs(down.position.y - endPoint.value.y) <= 50f) {
                                touchPosition = down.position
                                isTouching = true
                                do {
                                    val event = awaitPointerEvent()
                                    touchPosition = event.changes.first().position
                                } while (event.changes.any { it.pressed })
                                isTouching = false
                            }
                        }
                    }
                }) {
                val path = Path().apply {
                    moveTo(bulbCenterX, 0f)
                    if (isTouching) {
                        lineTo(touchPosition.x, touchPosition.y)
                    } else {
                        var x = bulbCenterX
                        var y = 0f
                        for (i in 0..yOffset.value.toInt() step 5) {
                            y = i.toFloat()
                            val phase = 2 * (yOffset.value - y) / yOffset.value * PI
                            val dx = waveAmplitude.value * sin(phase).toFloat()
                            lineTo(x + dx, y)
                        }
                    }
                }
                drawPath(
                    path = path,
                    color = Color.Black,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = Color.Black,
                    radius = 5.dp.toPx(),
                    center = if (isTouching) touchPosition else Offset(bulbCenterX, yOffset.value)
                )
            }
        }
    }


fun Offset.distanceTo(other: Offset): Float {
    val dx = x - other.x
    val dy = y - other.y
    return dx * dx + dy * dy  // Returns the squared distance
}


@Composable
fun LightBulbWithString() {
    var touchY by remember { mutableStateOf(100f) }
    var isTouching by remember { mutableStateOf(false) }
    val animateY by animateFloatAsState(
        targetValue = if (isTouching) touchY else 100f,
        animationSpec = spring()
    )
}
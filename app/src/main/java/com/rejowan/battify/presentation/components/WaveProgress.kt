package com.rejowan.battify.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rejowan.battify.R
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin


enum class WaveDirection { RIGHT, LEFT }

/**
 * Draws a wave progress indicator.
 *
 * @param modifier Modifier to be applied to the layout.
 * @param progress The vertical progress of the wave.
 * @param fillBrush or color The brush or color to fill the wave.
 * @param amplitudeRange Highest and lowest point of wave to animate.
 * @param amplitudeDuration Duration it takes for wave to go from lowest to highest amplitude and vice versa
 * @param waveSteps number of points which will be drawn on path to generate sine wave across width of component, lesser the steps, more soft wave would be. More the steps, boxy the wave would be. Number can be tweaked as per performance requirement.
 * @param phaseShiftDuration determines speed of wave moving horizontally
 * @param waveDirection left or right horizontal movement of sine wave
 */
@Composable
fun WaveProgress(
    modifier: Modifier = Modifier,
    progress: Float,
    fillBrush: Brush? = Brush.horizontalGradient(listOf(Color.Magenta, Color.Cyan)),
    color: Color? = null,
    amplitudeRange: ClosedFloatingPointRange<Float> = 30f..50f,
    waveSteps: Int = 20,
    waveFrequency: Int = 3,
    phaseShiftDuration: Int = 2000,
    amplitudeDuration: Int = 2000,
    waveDirection: WaveDirection = WaveDirection.RIGHT,
    isCharging: Boolean,
) {
    val path = remember { Path() } //reusing same path object to reduce object creation and gc calls
    val coroutineScope = rememberCoroutineScope()
    val phaseShift = remember { Animatable(0f) }
    val amplitude = remember { Animatable(amplitudeRange.start) }

    LaunchedEffect(amplitudeRange, amplitudeDuration) {
        coroutineScope.launch {
            amplitude.stop()
            amplitude.snapTo(amplitudeRange.start)
            amplitude.animateTo(
                targetValue = amplitudeRange.endInclusive,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = amplitudeDuration, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
    }

    LaunchedEffect(phaseShiftDuration) {
        coroutineScope.launch {
            phaseShift.stop()
            phaseShift.snapTo(0f)
            phaseShift.animateTo(
                targetValue = (2 * PI).toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = phaseShiftDuration, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .onSizeChanged { boxSize = it }
            .drawBehind {
                val yPos = (1 - progress) * size.height

                // Prepare and draw the wave path.
                val path = Path().apply {
                    val phaseShiftLocal = when (waveDirection) {
                        WaveDirection.RIGHT -> -phaseShift.value
                        WaveDirection.LEFT -> phaseShift.value
                    }
                    prepareSinePath(
                        path = this,
                        size = size,
                        frequency = waveFrequency,
                        amplitude = amplitude.value,
                        phaseShift = phaseShiftLocal,
                        position = yPos,
                        step = waveSteps
                    )
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }

                if (fillBrush != null) {
                    drawPath(path = path, brush = fillBrush, style = Fill)
                } else if (color != null) {
                    drawPath(path = path, color = color, style = Fill)
                } else {
                    throw IllegalArgumentException("Either fillBrush or color must be provided")
                }
            }

    ) {
        if (boxSize != IntSize.Zero) {
            val density = LocalDensity.current
            val halfHeightDp = with(density) { (boxSize.height / 2f).toDp() }


            Column(
                modifier = Modifier.offset(y = halfHeightDp - 24.dp),
            )
            {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "%",
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {

                    OutlinedCard(
                        colors = CardDefaults.elevatedCardColors().copy(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                    )
                    {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = if (isCharging) painterResource(id = R.drawable.ic_battery_charging) else painterResource(
                                    id = R.drawable.ic_battery_not_charging
                                ),
                                contentDescription = "Discharging",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.size(4.dp))

                            Text(
                                text = if (isCharging) "Charging" else "Discharging",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                    }

                }

            }


        }

    }
}


fun prepareSinePath(
    path: Path,
    size: Size,
    frequency: Int,
    amplitude: Float,
    phaseShift: Float,
    position: Float,
    step: Int
) {
    for (x in 0..size.width.toInt().plus(step) step step) {
        val y =
            position + amplitude * sin(x * frequency * Math.PI / size.width + phaseShift).toFloat()
        if (path.isEmpty)
            path.moveTo(x.toFloat(), max(0f, min(y, size.height)))
        else
            path.lineTo(x.toFloat(), max(0f, min(y, size.height)))
    }
}


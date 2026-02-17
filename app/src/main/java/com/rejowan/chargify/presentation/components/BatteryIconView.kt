package com.rejowan.chargify.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.rejowan.chargify.data.model.BatteryHealthLevel

@Composable
fun BatteryIconView(
    modifier: Modifier = Modifier,
    chargeLevel: Int,
    isCharging: Boolean,
    healthLevel: BatteryHealthLevel = BatteryHealthLevel.fromChargeLevel(chargeLevel)
) {
    val bgColor: Color
    val levelColor: Color

    when {
        isCharging -> {
            bgColor = Color(0xFF89EC9E)
            levelColor = Color(0xFF4DD86C)
        }
        healthLevel == BatteryHealthLevel.CRITICAL -> {
            bgColor = Color(0xFFEF5350)
            levelColor = Color(0xFFB71C1C)
        }
        healthLevel == BatteryHealthLevel.WARNING -> {
            bgColor = Color(0xFFFFCF96)
            levelColor = Color(0xFFF5AD56)
        }
        else -> {
            bgColor = Color(0xFF86B6F6)
            levelColor = Color(0xFF4E94F1)
        }
    }

    Canvas(modifier = modifier) {
        drawBatteryPortrait(
            chargeLevel = chargeLevel,
            bgColor = bgColor,
            levelColor = levelColor,
            radius = size.minDimension * 0.08f
        )
    }
}

private fun DrawScope.drawBatteryPortrait(
    chargeLevel: Int,
    bgColor: Color,
    levelColor: Color,
    radius: Float
) {
    val w = size.width
    val h = size.height
    val tipHeightPercent = 0.10f
    val tipWidthPercent = 0.50f

    // Tip rect (top center)
    val tipLeft = w * ((1f - tipWidthPercent) / 2f)
    val tipRight = w - tipLeft
    val tipBottom = h * tipHeightPercent
    val tipSize = Size(tipRight - tipLeft, tipBottom)

    // Body rect
    val bodyTop = tipBottom
    val bodySize = Size(w, h - bodyTop)

    val cornerRadius = CornerRadius(radius, radius)

    // Draw tip background
    val tipPath = Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(Offset(tipLeft, 0f), tipSize),
                topLeft = cornerRadius,
                topRight = cornerRadius,
                bottomLeft = CornerRadius.Zero,
                bottomRight = CornerRadius.Zero
            )
        )
    }
    drawPath(tipPath, bgColor)

    // Draw body background
    val bodyPath = Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(Offset(0f, bodyTop), bodySize),
                topLeft = CornerRadius.Zero,
                topRight = CornerRadius.Zero,
                bottomLeft = cornerRadius,
                bottomRight = cornerRadius
            )
        )
    }
    drawPath(bodyPath, bgColor)

    // Draw charge level
    if (chargeLevel > 0) {
        val effectiveLevel = chargeLevel.coerceIn(0, 100)

        if (effectiveLevel <= 90) {
            // Fill only body
            val fillHeight = bodySize.height * effectiveLevel / 90f
            val fillTop = bodyTop + bodySize.height - fillHeight

            val bl = if (effectiveLevel >= 77) cornerRadius else CornerRadius.Zero
            val br = if (effectiveLevel >= 77) cornerRadius else CornerRadius.Zero

            val fillPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(Offset(0f, fillTop), Size(w, fillHeight)),
                        topLeft = CornerRadius.Zero,
                        topRight = CornerRadius.Zero,
                        bottomLeft = bl,
                        bottomRight = br
                    )
                )
            }
            drawPath(fillPath, levelColor)
        } else {
            // Fill entire body
            val bodyFillPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(Offset(0f, bodyTop), bodySize),
                        topLeft = cornerRadius,
                        topRight = cornerRadius,
                        bottomLeft = cornerRadius,
                        bottomRight = cornerRadius
                    )
                )
            }
            drawPath(bodyFillPath, levelColor)

            // Fill tip proportionally (90-100%)
            val tipPercent = (effectiveLevel - 90) / 10f
            val tipFillHeight = tipSize.height * tipPercent
            val tipFillTop = tipSize.height - tipFillHeight

            val tipFillPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(Offset(tipLeft, tipFillTop), Size(tipSize.width, tipFillHeight)),
                        topLeft = if (tipPercent > 0.5f) cornerRadius else CornerRadius.Zero,
                        topRight = if (tipPercent > 0.5f) cornerRadius else CornerRadius.Zero,
                        bottomLeft = CornerRadius.Zero,
                        bottomRight = CornerRadius.Zero
                    )
                )
            }
            drawPath(tipFillPath, levelColor)
        }
    }
}

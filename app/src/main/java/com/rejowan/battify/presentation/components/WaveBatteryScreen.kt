package com.rejowan.battify.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun WaveBatteryScreen() {
    Box(
        modifier = Modifier
            .width(300.dp)
            .height(600.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val padding = 10f
            val cornerRadius = minOf(width, height) * 0.1f

            // 95% height for the battery body and 5% for the cap
            val bodyHeight = height * 0.94f

            // Calculate battery width and height (adjusted for padding)
            val batteryWidth = width - 2 * padding

            // Draw the battery body (starting from the bottom of the canvas)
            val bodyPath = Path().apply {
                // Start at the bottom-left corner inside the battery body
                moveTo(
                    x = padding,
                    y = height - padding - cornerRadius
                )

                // Draw the left vertical line (for the body)
                lineTo(
                    x = padding,
                    y = height - bodyHeight - padding + cornerRadius
                )

                // Draw the left bottom rounded corner
                quadraticTo(
                    x1 = padding,
                    y1 = height - bodyHeight - padding,
                    x2 = padding + cornerRadius,
                    y2 = height - bodyHeight - padding
                )

                // Draw the bottom horizontal line (for the body)
                lineTo(
                    x = batteryWidth + padding - cornerRadius,
                    y = height - bodyHeight - padding
                )

                // Draw the right bottom rounded corner
                quadraticTo(
                    x1 = batteryWidth + padding,
                    y1 = height - bodyHeight - padding,
                    x2 = batteryWidth + padding,
                    y2 = height - bodyHeight - padding + cornerRadius
                )

                // Draw the right vertical line (for the body)
                lineTo(
                    x = batteryWidth + padding,
                    y = height - padding - cornerRadius
                )

                // Draw the right top rounded corner
                quadraticTo(
                    x1 = batteryWidth + padding,
                    y1 = height - padding,
                    x2 = batteryWidth + padding - cornerRadius,
                    y2 = height - padding
                )

                // Draw the top horizontal line (for the body)
                lineTo(
                    x = padding + cornerRadius,
                    y = height - padding
                )

                // Draw the left top rounded corner
                quadraticTo(
                    x1 = padding,
                    y1 = height - padding,
                    x2 = padding,
                    y2 = height - padding - cornerRadius
                )
            }



            // Draw the body path
            drawPath(path = bodyPath, color = Color.Black, style = Stroke(width = 5f))

            val capHeight = height * 0.05f - padding
            val capWidth = batteryWidth * 0.4f
            // Bottom of the cap touches the top edge of the battery body.
            val capBottom = height - bodyHeight - padding
            val capTop = capBottom - capHeight
            val capLeft = padding + (batteryWidth - capWidth) / 2

            drawRoundRect(
                color = Color.Black,
                topLeft = Offset(capLeft, capTop),
                size = Size(capWidth, capHeight),
                cornerRadius = CornerRadius(cornerRadius / 2, cornerRadius / 2),
                style = Stroke(width = 5f)
            )

        }
    }
}


@Preview
@Composable
fun WaveBatteryScreenPreview() {
    WaveBatteryScreen()
}

//@Composable
//fun InteractiveRoundedRectangle() {
//
//}
//
//
//@Preview
//@Composable
//fun InteractiveRoundedRectanglePreview() {
//    InteractiveRoundedRectangle()
//}
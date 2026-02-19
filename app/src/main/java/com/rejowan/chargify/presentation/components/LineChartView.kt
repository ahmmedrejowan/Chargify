package com.rejowan.chargify.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.setPadding
import com.rejowan.chart.charts.LineChart
import com.rejowan.chart.data.Entry
import com.rejowan.chart.data.LineData
import com.rejowan.chart.data.LineDataSet

@Composable
fun LineChartView(
    data: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
    showFill: Boolean = true
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setPinchZoom(false)
                setDrawGridBackground(false)
                isDragEnabled = false
                setScaleEnabled(false)
                setTouchEnabled(false)
                legend.isEnabled = false
                xAxis.isEnabled = false
                axisLeft.isEnabled = false
                axisRight.isEnabled = false
                animateXY(1000, 1000)
                setPadding(0)
                setViewPortOffsets(4f, 0f, 0f, 0f)
                this.data = LineData()
            }
        },
        update = { chart ->
            val lineData = chart.data ?: return@AndroidView

            var set = lineData.getDataSetByIndex(0) as? LineDataSet
            if (set == null) {
                set = LineDataSet(mutableListOf(), "").apply {
                    mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                    cubicIntensity = 0.2f
                    setDrawFilled(showFill)
                    setDrawCircles(false)
                    setDrawValues(false)
                    lineWidth = 2f
                    color = lineColor.toArgb()
                    this.fillColor = fillColor.toArgb()
                    fillAlpha = (fillColor.alpha * 255).toInt()
                }
                lineData.addDataSet(set)
            }

            // Rebuild entries from data list
            set.clear()
            data.forEachIndexed { index, value ->
                set.addEntry(Entry(index.toFloat(), value))
            }

            lineData.notifyDataChanged()
            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    )
}

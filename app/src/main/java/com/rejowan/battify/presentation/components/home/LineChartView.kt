package com.rejowan.battify.presentation.components.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.setPadding
import com.rejowan.battify.vm.HomeViewModel
import com.rejowan.chart.charts.LineChart
import com.rejowan.chart.data.Entry
import com.rejowan.chart.data.LineData
import com.rejowan.chart.data.LineDataSet
import org.koin.androidx.compose.koinViewModel

@Composable
fun LineChartView(modifier: Modifier = Modifier.fillMaxWidth().height(70.dp), show: Int = 0, homeViewModel: HomeViewModel = koinViewModel()) {

    val currentUsage by homeViewModel.currentUsage.collectAsState()
    val batteryTemp by homeViewModel.batteryTemp.collectAsState()

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val chart = LineChart(context)
            chart.apply {
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
            }
            chart.data = LineData()
            chart
        },
        update = { chart ->
            val lineData = chart.data

            var set = lineData.getDataSetByIndex(0) as? LineDataSet
            if (set == null) {
                set = LineDataSet(mutableListOf(), "").apply {
                    mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                    cubicIntensity = 0.2f
                    setDrawFilled(true)
                    setDrawCircles(false)
                    setDrawValues(false)
                    lineWidth = 2f
                    color = Color.White.toArgb()
                }
                lineData.addDataSet(set)
            }

            val entry = (if (show == 0) currentUsage else batteryTemp?.first ?: 0f)?.let {
                Entry(
                    set.entryCount.toFloat(),
                    it
                )
            }
            set.addEntry(entry)

            if (set.entryCount > 20) {
                set.removeEntry(0)
                for (i in 0 until set.entryCount) {
                    val entry1 = set.getEntryForIndex(i)
                    entry1.x = i.toFloat()
                }
            }

            lineData.notifyDataChanged()
            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    )
}
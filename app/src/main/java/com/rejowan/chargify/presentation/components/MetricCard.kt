package com.rejowan.chargify.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    leadingIcon: Int,
    value: String,
    unit: String,
    chart: (@Composable () -> Unit)? = null,
    minValue: String? = null,
    avgValue: String? = null,
    maxValue: String? = null
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        CardTitle(
            title = title,
            leadingIcon = leadingIcon,
        )

        chart?.invoke()

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = buildAnnotatedString {
                    append("$value ")
                    withStyle(
                        style = SpanStyle(
                            fontSize = 14.sp,
                        )
                    ) {
                        append(unit)
                    }
                },
                modifier = Modifier,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                textAlign = TextAlign.Center
            )
        }

        if (minValue != null && avgValue != null && maxValue != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricLabel("Min", minValue)
                MetricLabel("Avg", avgValue)
                MetricLabel("Max", maxValue)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun MetricLabel(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

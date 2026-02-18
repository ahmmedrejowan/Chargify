package com.rejowan.chargify.presentation.screens.main.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rejowan.chargify.R
import com.rejowan.chargify.presentation.components.SectionHeader

data class ToolItem(
    val title: String,
    val description: String,
    val iconRes: Int,
    val accentColor: Color,
    val route: String
)

@Composable
fun ToolsSection(
    onToolClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tools = listOf(
        ToolItem(
            title = "Charging Alarms",
            description = "Get notified at custom battery levels. Set alerts for full charge, low battery, or any percentage you want.",
            iconRes = R.drawable.ic_alarm,
            accentColor = Color(0xFF4DD86C),
            route = "charging_alarms"
        ),
        ToolItem(
            title = "Charging History",
            description = "Track your charging patterns and sessions",
            iconRes = R.drawable.ic_history,
            accentColor = Color(0xFF64B5F6),
            route = "charging_history"
        ),
        ToolItem(
            title = "Battery Tips",
            description = "Expert tips to extend battery lifespan",
            iconRes = R.drawable.ic_tips,
            accentColor = Color(0xFFF5AD56),
            route = "battery_tips"
        ),
        ToolItem(
            title = "Screen Time",
            description = "See which apps you spend the most time in. Requires usage access permission.",
            iconRes = R.drawable.ic_apps,
            accentColor = Color(0xFF9575CD),
            route = "app_usage"
        )
    )

    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = "Tools")

        Spacer(modifier = Modifier.height(16.dp))

        // Featured tool card - Charging Alarms (full width)
        FeaturedToolCard(
            tool = tools[0],
            onClick = { onToolClick(tools[0].route) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Grid of tools (2 columns) - Row 1
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ToolCard(
                tool = tools[1],
                onClick = { onToolClick(tools[1].route) },
                modifier = Modifier.weight(1f)
            )
            ToolCard(
                tool = tools[2],
                onClick = { onToolClick(tools[2].route) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Screen Time card (full width with more detail)
        FeaturedToolCard(
            tool = tools[3],
            onClick = { onToolClick(tools[3].route) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        AboutCard()

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun FeaturedToolCard(
    tool: ToolItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            // Gradient accent strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(tool.accentColor, tool.accentColor.copy(alpha = 0.3f))
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(tool.accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = tool.iconRes),
                        contentDescription = tool.title,
                        tint = tool.accentColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tool.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = tool.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Arrow indicator
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_right),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ToolCard(
    tool: ToolItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
            .height(180.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(tool.accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = tool.iconRes),
                        contentDescription = tool.title,
                        tint = tool.accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Arrow indicator
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_right),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = tool.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = tool.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.2
            )
        }
    }
}

@Composable
private fun AboutCard(
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Chargify",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Made with ❤️",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

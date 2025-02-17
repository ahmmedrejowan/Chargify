package com.rejowan.battify.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rejowan.battify.di.homeModule
import com.rejowan.battify.presentation.components.WaveDirection
import com.rejowan.battify.presentation.components.WaveProgress
import com.rejowan.battify.ui.theme.AppTheme
import com.rejowan.battify.vm.HomeViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.KoinApplication


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(homeViewModel: HomeViewModel = koinViewModel()) {

    val isCharging = homeViewModel.isCharging.collectAsState()
    val chargeLevel by homeViewModel.chargeLevel.collectAsState()

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(text = "Battify")
            },
            actions = {
                IconButton(onClick = {

                }) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            }
        )

    }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {

            Box(
                modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .size(250.dp)
                        .aspectRatio(1f)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .border(5.dp, Color.White, CircleShape)
                ) {
                    WaveProgress(
                        progress = (chargeLevel?.toFloat()?.div(100)) ?: 0f,
                        modifier = Modifier.fillMaxSize(),
                        fillBrush = Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                if (isCharging.value == true) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                            )
                        ),
                        waveDirection = WaveDirection.RIGHT,
                        amplitudeRange = 20f..50f,
                        waveFrequency = 3,
                        waveSteps = 20,
                        phaseShiftDuration = 2000,
                        amplitudeDuration = 2000,
                        isCharging = isCharging.value
                    )
                }
            }

            Text(
                text = "Battery Status: ${if (isCharging.value == true) "Charging" else "Not Charging"}",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }


    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {

    KoinApplication(application = {
        modules(homeModule)
    }) {
        AppTheme {
            MainScreen(homeViewModel = koinViewModel())
        }
    }


}

@Composable
fun Activity(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Gray)
    ) {
        var progress by remember { mutableFloatStateOf(0.4f) }
        var minAmplitude by remember { mutableFloatStateOf(20f) }
        var maxAmplitude by remember { mutableFloatStateOf(50f) }
        var frequency by remember { mutableIntStateOf(3) }
        var steps by remember { mutableIntStateOf(20) }
        var phaseShiftDuration by remember { mutableIntStateOf(2000) }
        var amplitudeDuration by remember { mutableIntStateOf(2000) }
        var direction by remember { mutableStateOf(WaveDirection.RIGHT) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            Card(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .border(5.dp, Color.White, CircleShape)
            ) {
                WaveProgress(
                    progress = progress,
                    modifier = Modifier.fillMaxSize(),
                    fillBrush = Brush.horizontalGradient(listOf(Color.Magenta, Color.Cyan)),
                    waveDirection = direction,
                    amplitudeRange = minAmplitude..maxAmplitude,
                    waveFrequency = frequency,
                    waveSteps = steps,
                    phaseShiftDuration = phaseShiftDuration,
                    amplitudeDuration = amplitudeDuration,
                    isCharging = true
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.padding(start = 12.dp, end = 12.dp)) {
                Text(
                    "Progress", modifier = Modifier.align(Alignment.CenterVertically), color = Color.White
                )
                Slider(value = progress, onValueChange = { progress = it })
            }

            Row(modifier = Modifier.padding(start = 12.dp, end = 12.dp)) {
                Text(
                    "Min Amplitude", modifier = Modifier.align(Alignment.CenterVertically), color = Color.White
                )
                Slider(
                    value = minAmplitude, onValueChange = { minAmplitude = it }, valueRange = 10f..40f
                )
            }

            Row(modifier = Modifier.padding(start = 12.dp, end = 12.dp)) {
                Text(
                    "Max Amplitude", modifier = Modifier.align(Alignment.CenterVertically), color = Color.White
                )
                Slider(
                    value = maxAmplitude, onValueChange = { maxAmplitude = it }, valueRange = 40f..80f
                )
            }

            Row(modifier = Modifier.padding(start = 12.dp, end = 12.dp)) {
                Text(
                    "Frequency", modifier = Modifier.align(Alignment.CenterVertically), color = Color.White
                )
                Slider(
                    value = frequency.toFloat(),
                    onValueChange = { frequency = it.toInt() },
                    valueRange = 2f..10f,
                )
            }

            Row(modifier = Modifier.padding(start = 12.dp, end = 12.dp)) {
                Text(
                    "Steps", modifier = Modifier.align(Alignment.CenterVertically), color = Color.White
                )
                Slider(
                    value = steps.toFloat(),
                    onValueChange = { steps = it.toInt() },
                    valueRange = 2f..100f,
                )
            }

            Row(modifier = Modifier.padding(start = 12.dp, end = 12.dp)) {
                Text(
                    "PhaseShift Duration", modifier = Modifier.align(Alignment.CenterVertically), color = Color.White
                )
                Slider(
                    value = phaseShiftDuration.toFloat(),
                    onValueChange = { phaseShiftDuration = it.toInt() },
                    valueRange = 100f..5000f,
                )
            }

            Row(modifier = Modifier.padding(start = 12.dp, end = 12.dp)) {
                Text(
                    "Amplitude Duration", modifier = Modifier.align(Alignment.CenterVertically), color = Color.White
                )
                Slider(
                    value = amplitudeDuration.toFloat(),
                    onValueChange = { amplitudeDuration = it.toInt() },
                    valueRange = 100f..5000f,
                )
            }

            Row(modifier = Modifier.padding(start = 12.dp, end = 12.dp)) {
                Text(
                    "Direction: Left ", modifier = Modifier.align(Alignment.CenterVertically), color = Color.White
                )
                Switch(direction == WaveDirection.RIGHT, onCheckedChange = {
                    direction = if (direction == WaveDirection.RIGHT) WaveDirection.LEFT
                    else WaveDirection.RIGHT
                })
                Text(
                    " Right", modifier = Modifier.align(Alignment.CenterVertically), color = Color.White
                )
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    AppTheme {
//        Activity()
//    }
//}
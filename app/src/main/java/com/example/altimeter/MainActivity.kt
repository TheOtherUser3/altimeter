package com.example.altimeter

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min
import kotlin.math.pow

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var pressureSensor: Sensor? = null

    private var pressure by mutableStateOf(1013.25f)
    private var altitude by mutableStateOf(0.0)
    private var sensorAvailable by mutableStateOf(true)

    private var simulating by mutableStateOf(false)

    private val P0 = 1013.25

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

        if (pressureSensor == null) {
            sensorAvailable = false
        }

        setContent {
            AltimeterApp(
                pressure = pressure,
                altitude = altitude,
                sensorAvailable = sensorAvailable,
                simulating = simulating,
                onSimulate = { delta -> simulatePressure(delta) },
                onStopSim = { stopSimulation() }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (sensorAvailable) {
            pressureSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (sensorAvailable) {
            sensorManager.unregisterListener(this)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // Prevent sensor from overwriting simulated values
        if (simulating) return

        if (event?.sensor?.type == Sensor.TYPE_PRESSURE) {
            val p = event.values[0]
            pressure = p
            altitude = calculateAltitude(p.toDouble())
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun simulatePressure(delta: Float) {
        simulating = true
        val newP = (pressure + delta).coerceAtLeast(300f)
        pressure = newP
        altitude = calculateAltitude(newP.toDouble())
    }

    private fun stopSimulation() {
        simulating = false
        // Immediately update altitude using real sensor on exit
        // (This happens when the next sensor event arrives)
    }

    private fun calculateAltitude(P: Double): Double {
        return 44330 * (1 - (P / P0).pow(1.0 / 5.255))
    }
}

@Composable
fun AltimeterApp(
    pressure: Float,
    altitude: Double,
    sensorAvailable: Boolean,
    simulating: Boolean,
    onSimulate: (Float) -> Unit,
    onStopSim: () -> Unit
) {
    val backgroundColor = altitudeToColor(altitude)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = "ALTIMETER",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(16.dp))

            if (!sensorAvailable) {
                Text(
                    text = "No barometer detected. Using simulation only.",
                    fontSize = 18.sp,
                    color = Color.Yellow
                )
                Spacer(Modifier.height(12.dp))
            }

            if (simulating) {
                Text(
                    text = "SIMULATION ACTIVE",
                    fontSize = 18.sp,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
            }

            Text(
                text = "Pressure: ${String.format("%.2f", pressure)} hPa",
                fontSize = 20.sp,
                color = Color.White
            )

            Text(
                text = "Altitude: ${String.format("%.2f", altitude)} m",
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(Modifier.height(24.dp))

            Row {
                Button(onClick = { onSimulate(-2f) }) {
                    Text("Sim +Altitude")
                }
                Spacer(Modifier.width(16.dp))
                Button(onClick = { onSimulate(+2f) }) {
                    Text("Sim -Altitude")
                }
            }

            Spacer(Modifier.height(16.dp))

            if (simulating) {
                Button(onClick = { onStopSim() }) {
                    Text("Stop Simulation")
                }
            }
        }
    }
}

fun altitudeToColor(altitude: Double): Color {
    val darkness = min(1f, (altitude / 5000f).toFloat())
    val base = 0.15f + (0.6f * (1 - darkness))
    return Color(base, base, base)
}

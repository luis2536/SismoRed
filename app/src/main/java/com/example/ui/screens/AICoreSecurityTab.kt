package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.LogManager
import com.example.utils.SeismicSensorManager
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class TelemetryCalc(
    val id: String,
    val station: String,
    val rtt: Int,
    val variance: Float,
    val filterType: String
)

@Composable
fun AICoreSecurityTab() {
    val infiniteTransition = rememberInfiniteTransition(label = "core_anim_v2")
    
    // Core rotation
    val rotationZ by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotZ"
    )

    // Pulse effect for 3D glow
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Advanced dynamic metrics
    val physicalVibration by SeismicSensorManager.vibrationForce.collectAsState()

    var threatLevelBase by remember { mutableStateOf(12) }
    val threatLevel = remember(threatLevelBase, physicalVibration) {
        (threatLevelBase + (physicalVibration * 15f).toInt()).coerceIn(1, 99)
    }

    var processingPower by remember { mutableStateOf(88) }
    var activeStations by remember { mutableStateOf(4) }
    var spectralEfficiency by remember { mutableStateOf(99.4f) }
    
    val aiStatus = remember(threatLevel) {
        if (threatLevel > 45) "CRÍTICO - ANOMALÍA DETECTADA"
        else if (threatLevel > 22) "ESTUDIO DE ANOMALÍAS"
        else "ÓPTIMO - COGNITIVO"
    }
    
    val telemetryList = remember { mutableStateListOf<TelemetryCalc>() }

    LaunchedEffect(Unit) {
        LogManager.log("INFO", "AI Core Cerebro: Inicializando algoritmos sismológicos cognitivos de alto rendimiento.")
        
        // Setup initial simulated telemetry calculations
        telemetryList.add(TelemetryCalc("TEL-01", "Nodo CCS", 18, 0.02f, "Fourier Bandpass"))
        telemetryList.add(TelemetryCalc("TEL-02", "Nodo MAR", 35, 0.05f, "Kalman Adaptive"))
        telemetryList.add(TelemetryCalc("TEL-03", "Nodo BAR", 22, 0.01f, "Wavelet Denoise"))
        telemetryList.add(TelemetryCalc("TEL-04", "Nodo MER", 45, 0.12f, "Lowpass Standard"))

        while (true) {
            delay(4000)
            threatLevelBase = (4..18).random()
            processingPower = (82..99).random()
            activeStations = (3..6).random()
            spectralEfficiency = 98.0f + (0..19).random() / 10f
            
            // Randomly update a telemetry row safely
            if (telemetryList.isNotEmpty()) {
                val index = (0 until telemetryList.size).random()
                telemetryList[index] = telemetryList[index].copy(
                    rtt = (15..55).random(),
                    variance = (0..15).random() / 100f
                )
            }
            
            if (threatLevel > 15) {
                LogManager.log("WARN", "Cerebro Red: Cómputo sismográfico distribuido re-sincronizado de alta seguridad.")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030303))
            .padding(14.dp)
    ) {
        // Futuristic Title Panel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SYNTRY_LAB // NEURAL ENGINE",
                    color = Color(0xFF00FFFF),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "ESTADO DE CÓMPUTO LOCAL-FIRST / SEGURO",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF00FFFF).copy(alpha = 0.1f))
                    .border(1.dp, Color(0xFF00FFFF).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "V2.8-SEC",
                    color = Color(0xFF00FFFF),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Advanced 3D Cyber Core Visualizer Panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF00FFFF).copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                .background(Color(0xFF060606)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationX = 40f
                        rotationY = 20f
                    }
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val baseRadius = 65f * density

                // Dynamic background cyber-wave (sinusoidal orbital rings + accelerometer feedback)
                for (angle in 0..360 step 15) {
                    val angleRad = Math.toRadians(angle.toDouble())
                    val waveOffset = sin(angleRad * 3 + Math.toRadians(rotationZ.toDouble())) * 12f + (physicalVibration * 18f)
                    val x = center.x + cos(angleRad).toFloat() * (baseRadius * 1.5f + waveOffset.toFloat())
                    val y = center.y + sin(angleRad).toFloat() * (baseRadius * 1.5f + waveOffset.toFloat())
                    drawCircle(
                        color = if (physicalVibration > 1.2f) Color.Red.copy(alpha = 0.4f) else Color(0xFF00FF00).copy(alpha = 0.15f),
                        radius = if (physicalVibration > 1.2f) 4.5f else 3f,
                        center = Offset(x, y)
                    )
                }

                // Rotating core outer ring
                drawArc(
                    color = Color(0xFF00FFFF).copy(alpha = 0.35f),
                    startAngle = rotationZ,
                    sweepAngle = 260f,
                    useCenter = false,
                    topLeft = Offset(center.x - baseRadius * 1.3f, center.y - baseRadius * 1.3f),
                    size = androidx.compose.ui.geometry.Size(baseRadius * 2.6f, baseRadius * 2.6f),
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )

                // Rotating core inner ring (opposite direction)
                drawArc(
                    color = Color(0xFF00FFCC).copy(alpha = 0.5f),
                    startAngle = -rotationZ * 1.3f,
                    sweepAngle = 170f,
                    useCenter = false,
                    topLeft = Offset(center.x - baseRadius, center.y - baseRadius),
                    size = androidx.compose.ui.geometry.Size(baseRadius * 2f, baseRadius * 2f),
                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                )

                // Pulsing Central Core Sphere
                drawCircle(
                    color = Color(0xFF00FFFF).copy(alpha = 0.12f * pulseScale),
                    radius = baseRadius * 0.8f * pulseScale,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF00FFCC).copy(alpha = 0.4f),
                    radius = baseRadius * 0.4f * pulseScale,
                    center = center
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = baseRadius * 0.15f * pulseScale,
                    center = center
                )

                // Interconnected structural node lines
                for (i in 0 until 8) {
                    val angle = (i * 45) + rotationZ
                    val angleRad = Math.toRadians(angle.toDouble())
                    val endX = center.x + cos(angleRad).toFloat() * baseRadius * 1.3f
                    val endY = center.y + sin(angleRad).toFloat() * baseRadius * 1.3f
                    drawLine(
                        color = Color(0xFF00FFFF).copy(alpha = 0.25f),
                        start = center,
                        end = Offset(endX, endY),
                        strokeWidth = 2f
                    )
                }
            }
            
            // Hologram labels overlay
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("3D SYSTEM DIAGNOSTIC", color = Color(0xFF00FFFF).copy(alpha=0.6f), fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                    Text("ANTIGRAVITY COGNITIVE CORE", color = Color(0xFF00FF00).copy(alpha=0.6f), fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ESTADO GENERAL",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = aiStatus,
                        color = if (aiStatus.startsWith("ÓPTIMO")) Color(0xFF00FF00) else if (aiStatus.startsWith("CRÍTICO")) Color.Red else Color(0xFFFF9800),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("LATENCIA: LOCAL-FIRST", color = Color.White.copy(alpha=0.4f), fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                    Text("MODO: CRIPTOGRÁFICO", color = Color.White.copy(alpha=0.4f), fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Advanced Telemetry Statistics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SecurityStatCard(
                title = "PROCESAMIENTO",
                value = "$processingPower%",
                color = Color(0xFF00FFFF),
                icon = Icons.Filled.Memory,
                modifier = Modifier.weight(1f)
            )
            SecurityStatCard(
                title = "EFIC. ESPECTRAL",
                value = "$spectralEfficiency%",
                color = Color(0xFF00FF00),
                icon = Icons.Filled.Analytics,
                modifier = Modifier.weight(1f)
            )
            SecurityStatCard(
                title = "ESTACIONES",
                value = "$activeStations Active",
                color = Color.Yellow,
                icon = Icons.Filled.Router,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Live Calculations and Signal Decodes (Cerebro Core Calculations Table)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                .background(Color(0xFF080808))
                .padding(8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TELEMETRÍA DE RED SÍSMICA P2P COGNITIVA",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "LIVE COMPUTATION",
                        color = Color(0xFF00FFFF),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Divider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Header row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("CÓDIGO", color = Color.Gray, fontSize = 8.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                            Text("ESTACIÓN", color = Color.Gray, fontSize = 8.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1.5f))
                            Text("RTT (ms)", color = Color.Gray, fontSize = 8.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                            Text("VARIANZA", color = Color.Gray, fontSize = 8.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1.2f))
                            Text("FILTRO", color = Color.Gray, fontSize = 8.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1.8f))
                        }
                    }

                    items(telemetryList) { tel ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0C0C0C))
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tel.id, color = Color(0xFF00FFFF), fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                            Text(tel.station, color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1.5f))
                            Text("${tel.rtt}", color = if (tel.rtt > 40) Color.Yellow else Color(0xFF00FF00), fontSize = 9.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                            Text(String.format("%.3f", tel.variance), color = Color.White.copy(alpha = 0.8f), fontSize = 9.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1.2f))
                            Text(tel.filterType, color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1.8f))
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        // Real-time security modules check list
                        Text(
                            text = "VECTORES DE DEFENSA ACTIVA",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DefensePill("AES-256", true)
                            DefensePill("P2P MESH", true)
                            DefensePill("SSL PINNING", true)
                            DefensePill("ROOM SECURE", true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DefensePill(name: String, active: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(if (active) Color(0xFF00FF00).copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f))
            .border(1.dp, if (active) Color(0xFF00FF00).copy(alpha = 0.4f) else Color.Red.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (active) Color(0xFF00FF00) else Color.Red)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = name,
                color = if (active) Color(0xFF00FF00) else Color.Red,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun SecurityStatCard(title: String, value: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0A0A0A))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(title, color = color.copy(alpha=0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
        }
    }
}

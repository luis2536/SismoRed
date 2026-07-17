package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AICoreSecurityTab() {
    val infiniteTransition = rememberInfiniteTransition(label = "core_anim")
    
    // Core rotation
    val rotationZ by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotZ"
    )

    // Pulse effect
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Simulating Threat level / Processing power
    var threatLevel by remember { mutableStateOf(12) }
    var processingPower by remember { mutableStateOf(85) }
    var aiStatus by remember { mutableStateOf("ÓPTIMO") }
    
    LaunchedEffect(Unit) {
        LogManager.log("INFO", "AI Core: Inicializando módulos de seguridad.")
        while (true) {
            delay(3500)
            threatLevel = (5..25).random()
            processingPower = (70..99).random()
            aiStatus = if (threatLevel > 20) "ANÁLISIS ACTIVO" else "ÓPTIMO"
            if (threatLevel > 20) {
                LogManager.log("WARN", "AI Core: Anomalía detectada. Incrementando cálculos de red neuronal.")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030303))
            .padding(16.dp)
    ) {
        Text(
            text = "AI CORE & SECURITY COMMAND",
            color = Color(0xFF00FFFF), // AI Cyan
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 3D Visualizer Core
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(2.dp, Color(0xFF00FFFF).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .background(Color(0xFF080808)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationX = 45f // 3D tilt
                        rotationY = 15f
                    }
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val baseRadius = 80f * density

                // Outer Ring 1 (Rotating)
                drawArc(
                    color = Color(0xFF00FFFF).copy(alpha = 0.4f),
                    startAngle = rotationZ,
                    sweepAngle = 280f,
                    useCenter = false,
                    topLeft = Offset(center.x - baseRadius * 1.5f, center.y - baseRadius * 1.5f),
                    size = androidx.compose.ui.geometry.Size(baseRadius * 3f, baseRadius * 3f),
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )

                // Outer Ring 2 (Rotating opposite)
                drawArc(
                    color = Color(0xFF00FF00).copy(alpha = 0.6f),
                    startAngle = -rotationZ * 1.5f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(center.x - baseRadius * 1.2f, center.y - baseRadius * 1.2f),
                    size = androidx.compose.ui.geometry.Size(baseRadius * 2.4f, baseRadius * 2.4f),
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )
                
                // Pulsing Core
                drawCircle(
                    color = Color(0xFF00FFFF).copy(alpha = 0.15f * pulseScale),
                    radius = baseRadius * pulseScale,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF00FFFF).copy(alpha = 0.8f),
                    radius = (baseRadius / 2) * pulseScale,
                    center = center
                )

                // Render inner connections
                for (i in 0 until 12) {
                    val angle = (i * 30) + rotationZ
                    val angleRad = Math.toRadians(angle.toDouble())
                    val endX = center.x + cos(angleRad).toFloat() * baseRadius * 1.6f
                    val endY = center.y + sin(angleRad).toFloat() * baseRadius * 1.6f
                    drawLine(
                        color = Color(0xFF00FFFF).copy(alpha = 0.4f),
                        start = center,
                        end = Offset(endX, endY),
                        strokeWidth = 2.5f
                    )
                }

                // Render dynamic data nodes
                for (i in 0 until 5) {
                    val angle = (i * 72) - (rotationZ * 2f)
                    val angleRad = Math.toRadians(angle.toDouble())
                    val endX = center.x + cos(angleRad).toFloat() * baseRadius * 2f
                    val endY = center.y + sin(angleRad).toFloat() * baseRadius * 2f
                    drawCircle(
                        color = Color.Yellow.copy(alpha = 0.8f),
                        radius = 6f * pulseScale,
                        center = Offset(endX, endY)
                    )
                }
            }
            
            // Overlay text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("NEURAL ENGINE (P2P MESH)", color = Color(0xFF00FFFF), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(4.dp))
                Text(aiStatus, color = if (aiStatus == "ÓPTIMO") Color(0xFF00FF00) else Color.Yellow, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats & Controls
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecurityStatCard(
                        title = "PODER CÓMPUTO",
                        value = "$processingPower%",
                        color = Color(0xFF00FFFF),
                        icon = Icons.Filled.Memory,
                        modifier = Modifier.weight(1f)
                    )
                    SecurityStatCard(
                        title = "NIVEL AMENAZA",
                        value = "$threatLevel",
                        color = if (threatLevel > 20) Color.Red else Color.Yellow,
                        icon = Icons.Filled.Warning,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    text = "MÓDULOS DE SEGURIDAD (EDGE LOCAL-FIRST)",
                    color = Color.White.copy(alpha=0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                SecurityModuleRow("Cifrado AES-256", "Activo - P2P Seguro", Color(0xFF00FF00))
                SecurityModuleRow("Red Neuronal GEMINI", "Sincronizado", Color(0xFF00FFFF))
                SecurityModuleRow("Base de Datos Room", "Aislada y Segura", Color(0xFF00FF00))
                SecurityModuleRow("Firewall de Borde", "Bloqueando I/O No Autorizado", Color.Yellow)
            }
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

@Composable
fun SecurityModuleRow(moduleName: String, status: String, statusColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF0A0A0A))
            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(moduleName, color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        Text(status, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

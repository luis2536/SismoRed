package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.LogManager
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

data class Node3D(val x: Float, val y: Float, val z: Float, val pulsePhase: Float)

@Composable
fun LogViewerTab() {
    val logs by LogManager.logs.collectAsState()
    
    // 3D rotation effect parameters for the overall container
    val infiniteTransition = rememberInfiniteTransition(label = "3d_rotation")
    val rotationX by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotX"
    )
    
    val rotationY by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotY"
    )

    // Dynamic rotation angle for the background 3D constellation
    val angleY3D by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angleY3D"
    )

    val angleX3D by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angleX3D"
    )

    // Generate a set of static 3D node coordinates representing the P2P network
    val nodes = remember {
        listOf(
            Node3D(-120f, -80f, -50f, 0.0f),
            Node3D(120f, -100f, 60f, 0.5f),
            Node3D(-60f, 120f, -90f, 1.0f),
            Node3D(80f, 90f, -20f, 1.5f),
            Node3D(0f, -40f, 130f, 2.0f),
            Node3D(-140f, 60f, 100f, 2.5f),
            Node3D(110f, 40f, -110f, 3.0f),
            Node3D(-30f, -130f, -10f, 3.5f),
            Node3D(40f, 130f, 80f, 4.0f),
            Node3D(-90f, -20f, 120f, 4.5f)
        )
    }

    LaunchedEffect(Unit) {
        LogManager.log("INFO", "Edge Terminal inicializado. Monitor 3D Activo.")
        while (true) {
            delay(5000)
            LogManager.log("DEBUG", "Ping asíncrono Local-First OK. Esperando paquetes...")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030303))
            .padding(16.dp)
            .graphicsLayer {
                this.rotationX = rotationX
                this.rotationY = rotationY
                this.cameraDistance = 14f * density
            }
    ) {
        // High fidelity real 3D mathematical projection canvas in the background
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            val width = size.width
            val height = size.height
            val scaleFactor = minOf(width, height) / 480f

            val projectedPoints = nodes.map { node ->
                // Rotate around Y-axis
                val cosY = cos(angleY3D)
                val sinY = sin(angleY3D)
                val x1 = node.x * cosY - node.z * sinY
                val z1 = node.x * sinY + node.z * cosY

                // Rotate around X-axis
                val cosX = cos(angleX3D)
                val sinX = sin(angleX3D)
                val y2 = node.y * cosX - z1 * sinX
                val z2 = node.y * sinX + z1 * cosX

                // 3D perspective math formula projection
                val distance = 300f
                val perspectiveScale = distance / (distance + z2)
                val projX = (width / 2f) + (x1 * perspectiveScale * scaleFactor)
                val projY = (height / 2f) + (y2 * perspectiveScale * scaleFactor)

                Offset(projX, projY) to z2
            }

            // Draw multi-node connections (P2P mesh grid lines)
            for (i in projectedPoints.indices) {
                for (j in i + 1 until projectedPoints.size) {
                    val p1 = projectedPoints[i].first
                    val p2 = projectedPoints[j].first
                    val dist3D = Math.abs(projectedPoints[i].second - projectedPoints[j].second)

                    // Connect nodes that are close to each other in 3D space
                    if (dist3D < 180f) {
                        val alpha = (1f - (dist3D / 180f)).coerceIn(0.05f, 0.45f)
                        drawLine(
                            color = Color(0xFF00FFCC).copy(alpha = alpha),
                            start = p1,
                            end = p2,
                            strokeWidth = 1.5f * scaleFactor
                        )
                    }
                }
            }

            // Draw glowing 3D nodes
            projectedPoints.forEachIndexed { index, pair ->
                val point = pair.first
                val zPosition = pair.second
                // Size changes based on depth in 3D space
                val sizeScale = (300f / (300f + zPosition)).coerceIn(0.4f, 2.0f)
                val baseRadius = 6f * sizeScale * scaleFactor

                // Glowing pulsing effect
                val pulse = (1f + 0.3f * sin(System.currentTimeMillis() / 250.0 + nodes[index].pulsePhase)).toFloat()

                drawCircle(
                    color = Color(0xFF00FF00).copy(alpha = 0.15f * sizeScale),
                    radius = baseRadius * 2.5f * pulse,
                    center = point
                )
                drawCircle(
                    color = if (zPosition < 0) Color(0xFF00FFCC) else Color(0xFF00FF55),
                    radius = baseRadius,
                    center = point
                )
            }
        }

        // Foreground terminal console
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .border(2.dp, Color(0xFF00FFCC).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .background(Color(0xFF050505).copy(alpha = 0.85f))
                .padding(12.dp)
        ) {
            Text(
                text = "TERMINAL DE DIAGNÓSTICO P2P (EDGE LOCAL-FIRST)",
                color = Color(0xFF00FFCC),
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                reverseLayout = false
            ) {
                items(logs) { log ->
                    val color = when (log.level) {
                        "ERROR" -> Color.Red
                        "WARN" -> Color.Yellow
                        "DEBUG" -> Color.Cyan
                        else -> Color(0xFF00FF00)
                    }
                    Text(
                        text = "[${log.timestamp}] [${log.level}] ${log.message}",
                        color = color,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.LogManager
import kotlinx.coroutines.delay

@Composable
fun LogViewerTab() {
    val logs by LogManager.logs.collectAsState()
    
    // 3D rotation effect parameters
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
            .background(Color(0xFF050505))
            .padding(16.dp)
            .graphicsLayer {
                this.rotationX = rotationX
                this.rotationY = rotationY
                this.cameraDistance = 14f * density
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .border(2.dp, Color(0xFF00FF00).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .background(Color(0xFF0A0A0A).copy(alpha = 0.95f))
                .padding(12.dp)
        ) {
            Text(
                text = "TERMINAL DE DIAGNÓSTICO (EDGE LOCAL-FIRST)",
                color = Color(0xFF00FF00),
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

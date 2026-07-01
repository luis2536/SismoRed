package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceScanScreen(
    onNavigateBack: () -> Unit
) {
    var scanning by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(scanning) {
        if (scanning) {
            delay(2000) // Simulate Edge AI Face Processing
            scanResult = "MATCH FOUND: Civil Registrado [ID: 9X-214]"
            scanning = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Telemetría Facial Edge AI", color = NeonCyan, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = NeonCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MatrixDark)
            )
        },
        containerColor = MatrixDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Simulated Camera Preview Viewport
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .clip(CircleShape)
                    .background(GlassBackground)
                    .border(2.dp, if (scanning) NeonCyan else GlassBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (scanning) {
                    CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(250.dp), strokeWidth = 2.dp)
                    Text("PROCESANDO TENSORFLOW.JS...", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                } else if (scanResult != null) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Match", tint = NeonGreen, modifier = Modifier.size(100.dp))
                } else {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "Camera", tint = TextPrimary.copy(alpha=0.3f), modifier = Modifier.size(100.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (scanResult != null) {
                Text(scanResult!!, color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { scanResult = null },
                    colors = ButtonDefaults.buttonColors(containerColor = GlassBackground, contentColor = NeonCyan),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("NUEVO ESCANEO", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { scanning = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = MatrixDark),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("INICIAR CRUCE BIOMÉTRICO", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    }
}

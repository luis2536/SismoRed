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
import androidx.compose.ui.text.style.TextAlign
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
    var mode by remember { mutableStateOf("VIVO") } // "VIVO" or "FORENSE"

    LaunchedEffect(scanning) {
        if (scanning) {
            delay(2500) // Simulate Edge AI Face Processing
            scanResult = if (mode == "FORENSE") {
                "COINCIDENCIA (92%): Civil [ID: 7X-912]. Reconstrucción IA Exitosa (Daño Post-Mortem detectado)."
            } else {
                "COINCIDENCIA (99%): Civil [ID: 9X-214]. Estado Vital: Estable."
            }
            scanning = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escáner Biométrico IA", color = FlagYellow, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = FlagYellow)
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                RadioButton(
                    selected = mode == "VIVO",
                    onClick = { mode = "VIVO" },
                    colors = RadioButtonDefaults.colors(selectedColor = FlagBlue, unselectedColor = GlassBorder)
                )
                Text("Búsqueda Vivo", color = TextPrimary, modifier = Modifier.align(Alignment.CenterVertically))
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = mode == "FORENSE",
                    onClick = { mode = "FORENSE" },
                    colors = RadioButtonDefaults.colors(selectedColor = FlagRed, unselectedColor = GlassBorder)
                )
                Text("Análisis Forense IA", color = ErrorRed, modifier = Modifier.align(Alignment.CenterVertically))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Simulated Camera Preview Viewport
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .clip(CircleShape)
                    .background(GlassBackground)
                    .border(2.dp, if (scanning) FlagYellow else GlassBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (scanning) {
                    CircularProgressIndicator(color = FlagYellow, modifier = Modifier.size(250.dp), strokeWidth = 2.dp)
                    Text("PROCESANDO TENSORFLOW IA...", color = FlagYellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                } else if (scanResult != null) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Match", tint = if (mode == "FORENSE") FlagRed else FlagBlue, modifier = Modifier.size(100.dp))
                } else {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "Camera", tint = TextPrimary.copy(alpha=0.3f), modifier = Modifier.size(100.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (scanResult != null) {
                Text(scanResult!!, color = if (mode == "FORENSE") FlagRed else FlagBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                if (mode == "FORENSE") {
                    Text("NOTA: Reconstrucción craneofacial asistida por Gemini AI aplicada con éxito. Precisión 92%.", color = TextPrimary, fontSize = 12.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Button(
                    onClick = { scanResult = null },
                    colors = ButtonDefaults.buttonColors(containerColor = GlassBackground, contentColor = FlagYellow),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("NUEVO ESCANEO", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { scanning = true },
                    colors = ButtonDefaults.buttonColors(containerColor = FlagYellow, contentColor = MatrixDark),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("INICIAR CRUCE BIOMÉTRICO", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ResqReportEntity
import com.example.ui.theme.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMapOfflineScreen(
    onNavigateToChat: () -> Unit,
    onNavigateToFaceScan: () -> Unit
) {
    // Scaffold UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("SismoRedVen", 
                         color = FlagYellow, 
                         fontWeight = FontWeight.Bold,
                         letterSpacing = 2.sp,
                         fontSize = 18.sp) 
                },
                actions = {
                    IconButton(onClick = onNavigateToFaceScan) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = "Escáner Biométrico", tint = FlagBlue)
                    }
                    IconButton(onClick = onNavigateToChat) {
                        Icon(Icons.Filled.Chat, contentDescription = "Chat Global", tint = FlagRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MatrixDark,
                    titleContentColor = FlagYellow
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Panic SOS */ },
                containerColor = ErrorRed,
                contentColor = TextPrimary
            ) {
                Icon(Icons.Filled.Warning, "Botón SOS de Pánico")
            }
        },
        bottomBar = {
            BottomAppBar(
                containerColor = GlassBackground,
                contentColor = FlagYellow
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    IconButton(onClick = { /* Mapa */ }) {
                        Icon(Icons.Filled.Warning, contentDescription = "Mapa", tint = FlagYellow)
                    }
                    IconButton(onClick = { /* Registro */ }) {
                        Icon(Icons.Filled.Add, contentDescription = "Registro Voluntarios", tint = FlagBlue)
                    }
                    IconButton(onClick = onNavigateToFaceScan) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = "Escáner Biométrico", tint = FlagRed)
                    }
                    IconButton(onClick = onNavigateToChat) {
                        Icon(Icons.Filled.Chat, contentDescription = "Chat Global", tint = TextPrimary)
                    }
                }
            }
        },
        containerColor = MatrixDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // "Map" Placeholder (Offline Cached Map Concept)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassBackground)
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Warning, contentDescription = "Map Offline", tint = FlagYellow, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("MAPA OFFLINE (CACHÉ)", color = FlagYellow, fontWeight = FontWeight.Bold)
                    Text("Cargando polígonos locales...", color = TextPrimary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Telemetry Dashboard
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TelemetryCard(title = "ESTADO RED", value = "MALLA P2P ACTIVA", color = FlagBlue, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                TelemetryCard(title = "COORDENADAS", value = "LAT 10.48 | LON -66.90", color = FlagRed, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("REPORTES LOCALES", color = FlagBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // Fake List for Demonstration
            val dummyList = listOf(
                ResqReportEntity(1, "Sismo", "Magnitud 6.8 reportada en la zona central.", 10.48, -66.90, isSynced = false),
                ResqReportEntity(2, "Persona", "Solicitud de búsqueda por IA biométrica", 10.50, -66.92, isSynced = true)
            )

            LazyColumn {
                items(dummyList) { report ->
                    ReportCard(report)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun TelemetryCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(GlassBackground)
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(title, color = TextPrimary.copy(alpha=0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ReportCard(report: ResqReportEntity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(GlassBackground)
            .border(1.dp, if (report.isSynced) GlassBorder else ErrorRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(report.type.uppercase(), color = FlagYellow, fontWeight = FontWeight.Bold)
                Text(if (report.isSynced) "SINC" else "OFFLINE", color = if (report.isSynced) FlagBlue else ErrorRed, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(report.description, color = TextPrimary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Lat: ${report.latitude} | Lon: ${report.longitude}", color = TextPrimary.copy(alpha=0.6f), fontSize = 10.sp)
        }
    }
}

package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ResqReportEntity
import com.example.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlin.math.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GpsMapTab(
    userLat: Double,
    onUserLatChange: (Double) -> Unit,
    userLon: Double,
    onUserLonChange: (Double) -> Unit,
    scanningProgress: Float,
    compassHeading: Float,
    dbReportsList: List<ResqReportEntity>,
    gpsAccuracy: String,
    satelliteCount: Int,
    manualLatInput: String,
    onManualLatInputChange: (String) -> Unit,
    manualLonInput: String,
    onManualLonInputChange: (String) -> Unit,
    isManualInputEnabled: Boolean,
    onIsManualInputEnabledChange: (Boolean) -> Unit,
    locationPermissionsState: MultiplePermissionsState,
    onFetchCurrentLocation: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // GPS State and Accuracy Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GlassBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (locationPermissionsState.allPermissionsGranted) Color.Green else FlagYellow)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "ESTADO GPS: ${if (locationPermissionsState.allPermissionsGranted) "NATIVO ACTIVO" else "SIMULADO"}",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(FlagBlue.copy(alpha = 0.3f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("SATELLITE SYNC", color = FlagYellow, fontSize = 8.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TelemetryMiniBox(title = "PRECISIÓN SÍSMICA", value = gpsAccuracy, color = Color.Green, modifier = Modifier.weight(1.2f))
                        Spacer(modifier = Modifier.width(8.dp))
                        TelemetryMiniBox(title = "SATÉLITES EN LÍNEA", value = "$satelliteCount Activos", color = FlagYellow, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        TelemetryMiniBox(title = "ALTITUD", value = "412 metros", color = FlagBlue, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onFetchCurrentLocation,
                        colors = ButtonDefaults.buttonColors(containerColor = FlagBlue),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.GpsFixed, contentDescription = "Sincronizar", tint = TextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CENTRAR UBICACIÓN ACTUAL (GPS API)", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Tactical Vector Radar Canvas Mapping
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GlassBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("MAPA VECTORIAL TÁCTICO DE EMERGENCIAS", color = FlagYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Cálculo de azimut y mapeo local de balizas activas (Fuera de Línea)", color = TextPrimary.copy(alpha=0.7f), fontSize = 11.sp)
                    
                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color.Black, shape = RoundedCornerShape(8.dp))
                            .border(1.5.dp, FlagBlue.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val center = Offset(w / 2f, h / 2f)
                            val radius = min(w, h) / 2.2f

                            // Draw Radar lines and rings
                            drawCircle(FlagBlue.copy(alpha = 0.2f), radius = radius, center = center, style = Stroke(1.dp.toPx()))
                            drawCircle(FlagBlue.copy(alpha = 0.15f), radius = radius * 0.7f, center = center, style = Stroke(0.5.dp.toPx()))
                            drawCircle(FlagBlue.copy(alpha = 0.1f), radius = radius * 0.4f, center = center, style = Stroke(0.5.dp.toPx()))

                            // Grid Axis lines
                            drawLine(Color.DarkGray.copy(alpha = 0.4f), Offset(0f, center.y), Offset(w, center.y))
                            drawLine(Color.DarkGray.copy(alpha = 0.4f), Offset(center.x, 0f), Offset(center.x, h))

                            // Draw radar sweep
                            val angleRad = (scanningProgress * 2f * PI)
                            val sweepX = center.x + radius * cos(angleRad).toFloat()
                            val sweepY = center.y + radius * sin(angleRad).toFloat()
                            drawLine(
                                color = FlagBlue.copy(alpha = 0.6f),
                                start = center,
                                end = Offset(sweepX, sweepY),
                                strokeWidth = 1.5.dp.toPx()
                            )

                            // 1. Plot user glowing location pulse
                            val pulseRadius = (scanningProgress * 20.dp.toPx())
                            drawCircle(Color.Green.copy(alpha = 1f - scanningProgress), radius = pulseRadius, center = center)
                            drawCircle(Color.Green, radius = 5.dp.toPx(), center = center)

                            // 2. Plot nearby static beacons for venezuelan context
                            // We place mock signals around: Vargas (-66.88), Caracas (-66.89)
                            // Relative offset calculations based on coordinates
                            val beacons = listOf(
                                Pair(10.495, -66.885), // Beacon 1 (Yellow)
                                Pair(10.485, -66.895), // Beacon 2 (Red)
                                Pair(10.505, -66.878)  // Beacon 3 (Orange)
                            )

                            beacons.forEach { beacon ->
                                val dx = ((beacon.second - userLon) * 2000f).toFloat() * (radius / 100f)
                                val dy = (-((beacon.first - userLat) * 2000f)).toFloat() * (radius / 100f)
                                val pos = Offset(center.x + dx, center.y + dy)

                                // Check boundaries
                                val dist = sqrt(dx*dx + dy*dy)
                                if (dist < radius) {
                                    drawCircle(FlagYellow, radius = 4.dp.toPx(), center = pos)
                                    drawCircle(FlagYellow.copy(alpha = 0.3f), radius = 10.dp.toPx() * (1f - scanningProgress), center = pos)
                                }
                            }

                            // 3. Plot Dynamic Database reports
                            dbReportsList.forEach { report ->
                                val dx = ((report.longitude - userLon) * 2000f).toFloat() * (radius / 100f)
                                val dy = (-((report.latitude - userLat) * 2000f)).toFloat() * (radius / 100f)
                                val pos = Offset(center.x + dx, center.y + dy)

                                val dist = sqrt(dx*dx + dy*dy)
                                if (dist < radius) {
                                    drawCircle(Color.Cyan, radius = 5.dp.toPx(), center = pos)
                                    drawCircle(Color.Cyan.copy(alpha = 0.3f), radius = 12.dp.toPx() * (1f - scanningProgress), center = pos)
                                }
                            }

                            // 4. Draw Rotating Compass Dial on Top Right
                            val compassCenter = Offset(w - 40.dp.toPx(), 40.dp.toPx())
                            val compassRadius = 24.dp.toPx()
                            drawCircle(Color.Black, radius = compassRadius, center = compassCenter)
                            drawCircle(GlassBorder, radius = compassRadius, center = compassCenter, style = Stroke(1.dp.toPx()))

                            val headRad = Math.toRadians(compassHeading.toDouble())
                            val needleX = compassCenter.x + compassRadius * sin(headRad).toFloat()
                            val needleY = compassCenter.y - compassRadius * cos(headRad).toFloat()
                            val needleXOpp = compassCenter.x - compassRadius * sin(headRad).toFloat()
                            val needleYOpp = compassCenter.y + compassRadius * cos(headRad).toFloat()

                            drawLine(FlagRed, compassCenter, Offset(needleX, needleY), strokeWidth = 2.dp.toPx())
                            drawLine(TextPrimary, compassCenter, Offset(needleXOpp, needleYOpp), strokeWidth = 2.dp.toPx())
                        }

                        // Compass Heading Text Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Text(
                                "RUMBO: ${compassHeading.toInt()}° NNE • BRÚJULA ACTIVA",
                                color = FlagYellow,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Latitud Actual: ${String.format("%.6f", userLat)}° N", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Longitud Actual: ${String.format("%.6f", userLon)}° W", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Manual Coordinate Configuration Override
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GlassBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("CONTROL MANUAL DE COORDENADAS", color = FlagYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Switch(
                            checked = isManualInputEnabled,
                            onCheckedChange = { onIsManualInputEnabledChange(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = FlagYellow, checkedTrackColor = FlagBlue)
                        )
                    }

                    Text("Utilice esta opción si el GPS físico no tiene visibilidad a satélites o para realizar pruebas sobre el radar.", color = TextPrimary.copy(alpha=0.6f), fontSize = 10.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isManualInputEnabled) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = manualLatInput,
                                onValueChange = onManualLatInputChange,
                                label = { Text("Latitud (E.g. 10.49)", fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FlagYellow, cursorColor = FlagYellow),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = manualLonInput,
                                onValueChange = onManualLonInputChange,
                                label = { Text("Longitud (E.g. -66.89)", fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FlagYellow, cursorColor = FlagYellow),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val lat = manualLatInput.toDoubleOrNull()
                                val lon = manualLonInput.toDoubleOrNull()
                                if (lat != null && lon != null) {
                                    onUserLatChange(lat)
                                    onUserLonChange(lon)
                                    Toast.makeText(context, "Coordenadas actualizadas manualmente en el Radar", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Coordenadas inválidas", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FlagYellow),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Fijar Coordenadas Manuales", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetryMiniBox(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .border(0.5.dp, GlassBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(title, color = TextPrimary.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = color, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

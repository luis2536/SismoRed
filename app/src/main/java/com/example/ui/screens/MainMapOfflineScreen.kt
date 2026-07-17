package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ResqReportEntity
import com.example.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.*

data class WifiDevice(
    val ssid: String,
    val mac: String,
    val rssi: Int,
    val estimatedDistance: Double,
    val status: String,
    val sector: String
)

data class SeismicStation(
    val code: String,
    val name: String,
    val location: String,
    val lat: Double,
    val lon: Double,
    val type: String
)

data class GeologicalFault(
    val name: String,
    val zone: String,
    val historicalSismos: String,
    val lengthKm: Int,
    val riskLevel: String,
    val slipRate: String
)

val funvisisStationsList = listOf(
    SeismicStation("CAR", "Estación Cajigal", "Caracas, Dto. Capital", 10.51, -66.93, "Banda Ancha Tridimensional"),
    SeismicStation("BIR", "Estación Birongo", "Brión, Edo. Miranda", 10.48, -66.24, "Sismómetro Satelital P2P"),
    SeismicStation("BAU", "Estación El Baúl", "El Baúl, Edo. Cojedes", 8.96, -68.28, "Estación de Subsuelo Profundo"),
    SeismicStation("CUR", "Estación Curarigua", "Torres, Edo. Lara", 10.15, -69.95, "Red Sismológica Central"),
    SeismicStation("ALV", "Estación Altagracia", "Orituco, Edo. Guárico", 9.86, -66.30, "Sensor de Alta Frecuencia"),
    SeismicStation("LIG", "Estación La Grita", "Jáuregui, Edo. Táchira", 8.13, -71.98, "Acelerógrafo Transandino"),
    SeismicStation("PTE", "Estación Puerto Cruz", "Edo. Anzoátegui", 10.18, -64.63, "Monitoreo de Falla de El Pilar"),
    SeismicStation("MIG", "Estación San Miguel", "Edo. Trujillo", 9.75, -70.05, "Red de Falla de Boconó"),
    SeismicStation("SOC", "Estación Socopó", "Edo. Barinas", 8.24, -70.78, "Sensor de Subsidencia Activo")
)

val geologicalFaultsList = listOf(
    GeologicalFault(
        name = "Falla de Boconó",
        zone = "Andes Venezolanos (Táchira - Lara - Yaracuy)",
        historicalSismos = "Mérida (1812), El Tocuyo (1950)",
        lengthKm = 500,
        riskLevel = "CRÍTICO - DESPLAZAMIENTO ACTIVO",
        slipRate = "5.2 mm/año"
    ),
    GeologicalFault(
        name = "Falla de San Sebastián",
        zone = "Borde Costero Central (La Guaira - Caracas - Miranda)",
        historicalSismos = "Caracas (1812, 1967)",
        lengthKm = 480,
        riskLevel = "CRÍTICO - ALTO ACOPLE SÍSMICO",
        slipRate = "4.5 mm/año"
    ),
    GeologicalFault(
        name = "Falla de El Pilar",
        zone = "Serranía del Interior y Oriente (Sucre - Golfo de Paria)",
        historicalSismos = "Cariaco (1997), Cumaná (1929)",
        lengthKm = 350,
        riskLevel = "CRÍTICO - ALTA VELOCIDAD DE DESLIZAMIENTO",
        slipRate = "8.0 mm/año"
    )
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MainMapOfflineScreen(
    onNavigateToChat: () -> Unit,
    onNavigateToFaceScan: () -> Unit,
    onNavigateToAI: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }
    
    // Test parameters
    var userLat by remember { mutableStateOf(10.49) }
    var userLon by remember { mutableStateOf(-66.89) }
    var showScanAnimation by remember { mutableStateOf(false) }
    var scanningProgress by remember { mutableStateOf(0f) }

    // Room Database integration
    val db = remember { Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "resq-net-db").build() }
    val reportDao = remember { db.resqReportDao() }

    // Dynamic counters for Aquí Estoy Venezuela (Baseline values + dynamic additions)
    var dynamicReportedCount by remember { mutableStateOf(66834) }
    var dynamicSearchedCount by remember { mutableStateOf(63760) }
    var dynamicLocatedCount by remember { mutableStateOf(3074) }

    // List of reports from local SQLite (Room)
    val dbReportsList = remember { mutableStateListOf<ResqReportEntity>() }

    // Search query for "Aquí Estoy" search box
    var searchQuery by remember { mutableStateOf("") }
    
    // Register Dialog state
    var showRegisterDialog by remember { mutableStateOf(false) }
    var registerName by remember { mutableStateOf("") }
    var registerLocationText by remember { mutableStateOf("") }
    var registerStatus by remember { mutableStateOf("Buscado") } // "Buscado", "Localizado", "Reportado"
    var registerDescription by remember { mutableStateOf("") }

    // GPS location states
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    var gpsAccuracy by remember { mutableStateOf("Excelente (GPS)") }
    var satelliteCount by remember { mutableStateOf(12) }
    var compassHeading by remember { mutableStateOf(45f) }
    var manualLatInput by remember { mutableStateOf("10.49") }
    var manualLonInput by remember { mutableStateOf("-66.89") }
    var isManualInputEnabled by remember { mutableStateOf(false) }

    // Acoustic sound wave life detector state
    var micSensitivity by remember { mutableStateOf(65f) }
    var isRecordingMic by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0) }
    var savedRecordingsCount by remember { mutableStateOf(0) }
    val lastSavedRecordings = remember { mutableStateListOf<String>() }
    var isPlayingSaved by remember { mutableStateOf(false) }
    var playbackProgress by remember { mutableStateOf(0f) }
    var alertThresholdExceeded by remember { mutableStateOf(false) }
    var waveTick by remember { mutableStateOf(0f) }

    // Wi-Fi / Radio signal device scanner state
    var isWifiScanning by remember { mutableStateOf(false) }
    var scanCompleted by remember { mutableStateOf(false) }
    val wifiDevices = remember { mutableStateListOf<WifiDevice>() }

    // Fetch reports from Room Database in real-time
    LaunchedEffect(Unit) {
        reportDao.getAllReports().collectLatest { reports ->
            dbReportsList.clear()
            dbReportsList.addAll(reports)
            
            val customReported = reports.size
            val customSearched = reports.count { it.type.lowercase().contains("buscado") }
            val customLocated = reports.count { it.type.lowercase().contains("localizado") }
            
            dynamicReportedCount = 66834 + customReported
            dynamicSearchedCount = 63760 + customSearched
            dynamicLocatedCount = 3074 + customLocated
        }
    }

    // Connect to native Play Services GPS location if permission is granted
    LaunchedEffect(locationPermissionsState.allPermissionsGranted, isManualInputEnabled) {
        if (locationPermissionsState.allPermissionsGranted && !isManualInputEnabled) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        userLat = location.latitude
                        userLon = location.longitude
                        manualLatInput = String.format("%.5f", location.latitude)
                        manualLonInput = String.format("%.5f", location.longitude)
                        gpsAccuracy = "+/- ${String.format("%.1f", location.accuracy)} metros"
                        satelliteCount = if (location.accuracy < 10) 14 else 9
                    }
                }
            } catch (e: SecurityException) {
                // permission exception
            }
        }
    }

    // Rotate compass needle dynamically
    LaunchedEffect(Unit) {
        while (true) {
            delay(120)
            if (isRecordingMic) {
                compassHeading = (compassHeading + 1f) % 360f
            } else {
                // drift natural drift heading
                compassHeading = (compassHeading + (sin(System.currentTimeMillis() / 2200.0) * 0.4f).toFloat() + 360f) % 360f
            }
        }
    }

    // Wave/Sensitivity Dynamic Tick
    LaunchedEffect(isRecordingMic, isPlayingSaved) {
        while (true) {
            delay(80)
            if (isRecordingMic) {
                waveTick += 0.5f
                alertThresholdExceeded = (micSensitivity > 40f) && (sin(waveTick.toDouble()).absoluteValue > 0.75)
            } else if (isPlayingSaved) {
                waveTick += 0.3f
                alertThresholdExceeded = false
            } else {
                waveTick += 0.05f
                alertThresholdExceeded = false
            }
        }
    }

    // Recording seconds timer
    LaunchedEffect(isRecordingMic) {
        if (isRecordingMic) {
            recordingDuration = 0
            while (isRecordingMic) {
                delay(1000)
                recordingDuration++
            }
        }
    }

    // Playback duration timer
    LaunchedEffect(isPlayingSaved) {
        if (isPlayingSaved) {
            playbackProgress = 0f
            while (playbackProgress < 1f && isPlayingSaved) {
                delay(100)
                playbackProgress += 0.05f
            }
            isPlayingSaved = false
            playbackProgress = 0f
        }
    }

    // Wireless WiFi scanner simulation
    LaunchedEffect(isWifiScanning) {
        if (isWifiScanning) {
            delay(2200)
            wifiDevices.clear()
            wifiDevices.add(WifiDevice("Sobr_Cel_A12", "E4:A3:82:11:F5:BC", -54, 1.9, "EMISIÓN SOS ACTIVA - SEÑAL FUERTE", "Bajo Escombros - Sector Vargas"))
            wifiDevices.add(WifiDevice("Sobr_Reloj_Fit5", "9C:D6:F1:82:44:A2", -79, 7.3, "CONEXIÓN DE ENLACE - SEÑAL REGULAR", "Sótano Colapsado - Sector Vargas"))
            wifiDevices.add(WifiDevice("Router_Comunal_P2P", "FF:EE:DD:CC:BB:AA", -68, 4.5, "NODO MALLA ACTIVO - PUENTE SATELITAL", "Refugio Temporal 1 - Caracas"))
            wifiDevices.add(WifiDevice("Beacon_Rescate_Aero", "00:11:22:33:44:55", -42, 0.9, "SEÑAL DE BALIZA DE EMERGENCIA - CRÍTICO", "Puesto Médico Avanzado"))
            isWifiScanning = false
            scanCompleted = true
        }
    }

    // Start periodic scanning animation for telemetry
    LaunchedEffect(Unit) {
        while (true) {
            delay(150)
            scanningProgress = (scanningProgress + 0.05f) % 1f
        }
    }

    // Scaffold UI with Venezuelan Tricolor Theme Accents
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Small Venezuelan Flag visual tag
                        Row(modifier = Modifier.height(20.dp).width(30.dp).border(0.5.dp, Color.White)) {
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(FlagYellow))
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(FlagBlue))
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(FlagRed))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "SismoRedVen", 
                            color = FlagYellow, 
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            fontSize = 20.sp
                        ) 
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToFaceScan) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = "Escáner Biométrico", tint = FlagYellow)
                    }
                    IconButton(onClick = {
                        // WhatsApp emergency broadcast intent
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "¡ALERTA SISMOREDVEN! Reportando coordenadas de emergencia: LAT $userLat | LON $userLon. Estado del sector: ALERTA SÍSMICA.")
                            type = "text/plain"
                            setPackage("com.whatsapp")
                        }
                        try {
                            context.startActivity(sendIntent)
                        } catch (e: Exception) {
                            val fallbackIntent = Intent.createChooser(sendIntent, "Compartir Reporte Vía WhatsApp")
                            context.startActivity(fallbackIntent)
                        }
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Compartir WhatsApp", tint = FlagBlue)
                    }
                    IconButton(onClick = onNavigateToChat) {
                        Icon(Icons.Filled.Chat, contentDescription = "Chat Satelital", tint = FlagRed)
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
                onClick = { 
                    // Share Quick SOS Alert
                    val smsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:911")).apply {
                        putExtra("sms_body", "SOS SISMOREDVEN: Requiere asistencia inmediata en LAT $userLat | LON $userLon")
                    }
                    try {
                        context.startActivity(smsIntent)
                    } catch (e: Exception) {
                        // fallback
                    }
                },
                containerColor = FlagRed,
                contentColor = TextPrimary,
                modifier = Modifier.border(2.dp, FlagYellow, CircleShape)
            ) {
                Icon(Icons.Filled.Warning, "Botón de Pánico SOS")
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MatrixDark,
                contentColor = FlagYellow,
                modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.Assessment, contentDescription = "Estadísticas") },
                    label = { Text("Aquí Estoy", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = FlagYellow, unselectedIconColor = TextPrimary.copy(alpha=0.6f))
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.MyLocation, contentDescription = "Mapa GPS") },
                    label = { Text("Mapa GPS", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.Green, unselectedIconColor = TextPrimary.copy(alpha=0.6f))
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Filled.Sensors, contentDescription = "Detector") },
                    label = { Text("Detector", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = FlagYellow, unselectedIconColor = TextPrimary.copy(alpha=0.6f))
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Filled.Timeline, contentDescription = "Sismología") },
                    label = { Text("Sismología", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = FlagBlue, unselectedIconColor = TextPrimary.copy(alpha=0.6f))
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Filled.Healing, contentDescription = "Recursos") },
                    label = { Text("Recursos", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = FlagRed, unselectedIconColor = TextPrimary.copy(alpha=0.6f))
                )
                NavigationBarItem(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    icon = { Icon(Icons.Filled.Terminal, contentDescription = "Terminal") },
                    label = { Text("Terminal", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF00FF00), unselectedIconColor = TextPrimary.copy(alpha=0.6f))
                )
                NavigationBarItem(
                    selected = selectedTab == 6,
                    onClick = { selectedTab = 6 },
                    icon = { Icon(Icons.Filled.Security, contentDescription = "AI Core") },
                    label = { Text("AI Core", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF00FFFF), unselectedIconColor = TextPrimary.copy(alpha=0.6f))
                )
            }
        },
        containerColor = MatrixDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            
            // Tab Row for Clean Separation
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = GlassBackground,
                contentColor = FlagYellow,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = when(selectedTab) {
                            0 -> FlagYellow
                            1 -> Color.Green
                            2 -> FlagYellow
                            3 -> FlagBlue
                            4 -> FlagRed
                            5 -> Color(0xFF00FF00) // Terminal Green
                            else -> Color(0xFF00FFFF) // AI Cyan
                        }
                    )
                }
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Aquí Estoy", fontSize = 10.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Mapa GPS", fontSize = 10.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Detector", fontSize = 10.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Sismología", fontSize = 10.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }, text = { Text("Recursos", fontSize = 10.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTab == 5, onClick = { selectedTab = 5 }, text = { Text("Terminal", fontSize = 10.sp, fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTab == 6, onClick = { selectedTab = 6 }, text = { Text("AI Core", fontSize = 10.sp, fontWeight = FontWeight.Bold) })
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> {
                    StatsAndManualTab(
                        dbReportsList = dbReportsList,
                        dynamicReportedCount = dynamicReportedCount,
                        dynamicSearchedCount = dynamicSearchedCount,
                        dynamicLocatedCount = dynamicLocatedCount,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        showRegisterDialog = showRegisterDialog,
                        onShowRegisterDialogChange = { showRegisterDialog = it },
                        registerName = registerName,
                        onRegisterNameChange = { registerName = it },
                        registerLocationText = registerLocationText,
                        onRegisterLocationTextChange = { registerLocationText = it },
                        registerDescription = registerDescription,
                        onRegisterDescriptionChange = { registerDescription = it },
                        registerStatus = registerStatus,
                        onRegisterStatusChange = { registerStatus = it },
                        onSaveReport = {
                            scope.launch {
                                val newReport = ResqReportEntity(
                                    type = "Persona_${registerStatus}",
                                    description = "Nombre: $registerName. Sector: $registerLocationText. Detalle: $registerDescription",
                                    latitude = userLat,
                                    longitude = userLon,
                                    isSynced = false
                                )
                                reportDao.insertReport(newReport)
                                Toast.makeText(context, "¡Reporte guardado localmente! Sincronización en cola.", Toast.LENGTH_LONG).show()
                                showRegisterDialog = false
                                // Reset fields
                                registerName = ""
                                registerLocationText = ""
                                registerDescription = ""
                            }
                        }
                    )
                }

                1 -> {
                    GpsMapTab(
                        userLat = userLat,
                        onUserLatChange = { userLat = it },
                        userLon = userLon,
                        onUserLonChange = { userLon = it },
                        scanningProgress = scanningProgress,
                        compassHeading = compassHeading,
                        dbReportsList = dbReportsList,
                        gpsAccuracy = gpsAccuracy,
                        satelliteCount = satelliteCount,
                        manualLatInput = manualLatInput,
                        onManualLatInputChange = { manualLatInput = it },
                        manualLonInput = manualLonInput,
                        onManualLonInputChange = { manualLonInput = it },
                        isManualInputEnabled = isManualInputEnabled,
                        onIsManualInputEnabledChange = { isManualInputEnabled = it },
                        locationPermissionsState = locationPermissionsState,
                        onFetchCurrentLocation = {
                            if (locationPermissionsState.allPermissionsGranted) {
                                try {
                                    fusedLocationClient.getCurrentLocation(
                                        Priority.PRIORITY_HIGH_ACCURACY,
                                        null
                                    ).addOnSuccessListener { location ->
                                        if (location != null) {
                                            userLat = location.latitude
                                            userLon = location.longitude
                                            manualLatInput = String.format("%.5f", location.latitude)
                                            manualLonInput = String.format("%.5f", location.longitude)
                                            gpsAccuracy = "+/- ${String.format("%.1f", location.accuracy)} metros"
                                        }
                                    }
                                } catch (e: SecurityException) {
                                    // exception
                                }
                            } else {
                                locationPermissionsState.launchMultiplePermissionRequest()
                            }
                        }
                    )
                }

                2 -> {
                    // TAB 0: ACOUSTIC LIFE DETECTOR & WIFI WIRELESS SURVIVOR SCANNER
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            // Acoustic Wave Biosensor
                            Card(
                                colors = CardDefaults.cardColors(containerColor = GlassBackground),
                                modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("BIOSENSOR ACÚSTICO DE ESCOMBROS (VÍA MICRÓFONO)", color = FlagYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Sensibilidad en tiempo real para detección de respiración y pulsaciones", color = TextPrimary.copy(alpha=0.7f), fontSize = 11.sp)
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Acoustic Wave Dynamic Simulation Canvas
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(115.dp)
                                            .background(Color.Black)
                                            .border(1.dp, FlagYellow.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val width = size.width
                                            val height = size.height
                                            val midY = height / 2f
                                            val points = 80
                                            val path = Path()
                                            path.moveTo(0f, midY)
                                            
                                            for (i in 0..points) {
                                                val x = (width / points) * i
                                                val angle = (i.toFloat() / points) * (2f * PI * 4f) + waveTick
                                                
                                                val amplitudeMultiplier = when {
                                                    isRecordingMic -> (micSensitivity / 100f) * (35.dp.toPx() + (if (alertThresholdExceeded) 15.dp.toPx() else 0f))
                                                    isPlayingSaved -> 25.dp.toPx() * (1f - playbackProgress)
                                                    else -> 8.dp.toPx()
                                                }
                                                
                                                val noise = if (isRecordingMic) {
                                                    (sin(angle * 3.5).toFloat() * 12f * (if (alertThresholdExceeded) 1.6f else 0.5f))
                                                } else {
                                                    0f
                                                }
                                                
                                                val y = midY + (sin(angle).toFloat() * amplitudeMultiplier) + noise
                                                path.lineTo(x, y)
                                            }
                                            
                                            drawPath(
                                                path = path,
                                                color = when {
                                                    isRecordingMic -> if (alertThresholdExceeded) FlagRed else FlagYellow
                                                    isPlayingSaved -> FlagBlue
                                                    else -> Color.Green.copy(alpha = 0.7f)
                                                },
                                                style = Stroke(width = 2.5.dp.toPx())
                                            )
                                        }
                                        
                                        // Top Bar indicators
                                        Box(modifier = Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.TopStart) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            when {
                                                                isRecordingMic -> if (alertThresholdExceeded) FlagRed else Color.Red
                                                                isPlayingSaved -> FlagBlue
                                                                else -> Color.Green
                                                            }
                                                        )
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = when {
                                                        isRecordingMic -> "MICRÓFONO ACTIVO: MONITOREANDO (${recordingDuration}s)"
                                                        isPlayingSaved -> "REPRODUCIENDO REGISTRO GUARDADO... (${(playbackProgress * 100).toInt()}%)"
                                                        else -> "BIOSENSOR EN ESPERA - COORDENADAS LISTAS"
                                                    },
                                                    color = TextPrimary,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        
                                        if (alertThresholdExceeded) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(FlagRed.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "¡ALERTA! SEÑAL DE VIDA DETECTADA (VOZ/LATIDO)",
                                                    color = FlagYellow,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 11.sp,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier
                                                        .background(Color.Black.copy(alpha = 0.85f))
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                        .border(1.dp, FlagYellow, RoundedCornerShape(4.dp))
                                                )
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Sensitivity Slider Controls
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("SENSIBILIDAD DE SENSOR SÍSMICO", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text("${micSensitivity.toInt()} dB", color = FlagYellow, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    }
                                    
                                    Slider(
                                        value = micSensitivity,
                                        onValueChange = { micSensitivity = it },
                                        valueRange = 10f..100f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = FlagYellow,
                                            activeTrackColor = FlagYellow,
                                            inactiveTrackColor = GlassBorder
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    
                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    // Recording / Saving & Playback controls
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Button 1: Start/Stop Recording
                                        Button(
                                            onClick = {
                                                isRecordingMic = !isRecordingMic
                                                if (isRecordingMic) {
                                                    isPlayingSaved = false
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isRecordingMic) FlagRed else GlassBackground
                                            ),
                                            modifier = Modifier.weight(1f).border(1.dp, if (isRecordingMic) FlagRed else FlagYellow, RoundedCornerShape(8.dp)),
                                            contentPadding = PaddingValues(vertical = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isRecordingMic) Icons.Filled.Stop else Icons.Filled.Mic,
                                                contentDescription = "Grabar Micrófono",
                                                tint = TextPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (isRecordingMic) "DETENER" else "GRABAR", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                                        }
                                        
                                        // Button 2: Save Recording
                                        Button(
                                            onClick = {
                                                if (recordingDuration > 0) {
                                                    savedRecordingsCount++
                                                    lastSavedRecordings.add("REGISTRO_BIOSENSOR_00$savedRecordingsCount.WAV (${recordingDuration}s)")
                                                } else {
                                                    savedRecordingsCount++
                                                    lastSavedRecordings.add("REGISTRO_BIOSENSOR_00$savedRecordingsCount.WAV (5s)")
                                                }
                                                isRecordingMic = false
                                            },
                                            enabled = isRecordingMic || recordingDuration > 0,
                                            colors = ButtonDefaults.buttonColors(containerColor = FlagBlue),
                                            modifier = Modifier.weight(1.2f),
                                            contentPadding = PaddingValues(vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Filled.Save, "Guardar", modifier = Modifier.size(16.dp), tint = TextPrimary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("GUARDAR REG", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                                        }
                                        
                                        // Button 3: Play last recording
                                        Button(
                                            onClick = {
                                                if (lastSavedRecordings.isNotEmpty()) {
                                                    isPlayingSaved = true
                                                    isRecordingMic = false
                                                }
                                            },
                                            enabled = lastSavedRecordings.isNotEmpty() && !isRecordingMic && !isPlayingSaved,
                                            colors = ButtonDefaults.buttonColors(containerColor = FlagYellow),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Filled.PlayArrow, "Reproducir", modifier = Modifier.size(16.dp), tint = Color.Black)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("REPROD", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    
                                    if (lastSavedRecordings.isNotEmpty()) {
                                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = GlassBorder)
                                        Text("REGISTROS GUARDADOS BAJO ESCOMBROS", color = FlagYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        
                                        // List the saved audios
                                        lastSavedRecordings.forEachIndexed { index, wavName ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp)
                                                    .background(GlassBackground.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                    .border(0.5.dp, GlassBorder, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Filled.VolumeUp, "Audio", tint = FlagBlue, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(wavName, color = TextPrimary, fontSize = 10.sp)
                                                }
                                                Text("LISTO / ANALIZADO", color = Color.Green, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            // Wi-Fi / Radio Device Scanner for Buried Survivors
                            Card(
                                colors = CardDefaults.cardColors(containerColor = GlassBackground),
                                modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("ESCÁNER DE DISPOSITIVOS WI-FI & P2P", color = FlagYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Búsqueda de ondas de radio de teléfonos de sobrevivientes atrapados", color = TextPrimary.copy(alpha=0.7f), fontSize = 11.sp)
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Simulated Circular Radar Sweep Canvas
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(130.dp)
                                                .background(Color.Black, shape = CircleShape)
                                                .border(1.5.dp, FlagBlue.copy(alpha = 0.6f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Canvas(modifier = Modifier.fillMaxSize()) {
                                                val center = Offset(size.width / 2f, size.height / 2f)
                                                val radius = size.width / 2f
                                                
                                                // Grid rings
                                                drawCircle(FlagBlue.copy(alpha = 0.15f), radius = radius * 0.75f, center = center)
                                                drawCircle(FlagBlue.copy(alpha = 0.1f), radius = radius * 0.5f, center = center)
                                                drawCircle(FlagBlue.copy(alpha = 0.05f), radius = radius * 0.25f, center = center)
                                                
                                                // Grid lines
                                                drawLine(Color.DarkGray.copy(alpha = 0.4f), Offset(0f, center.y), Offset(size.width, center.y))
                                                drawLine(Color.DarkGray.copy(alpha = 0.4f), Offset(center.x, 0f), Offset(center.x, size.height))
                                                
                                                // Radar sweep line
                                                val angleRad = (scanningProgress * 2f * PI)
                                                val endX = center.x + radius * cos(angleRad).toFloat()
                                                val endY = center.y + radius * sin(angleRad).toFloat()
                                                
                                                drawLine(
                                                    color = if (isWifiScanning) FlagYellow else FlagBlue.copy(alpha = 0.8f),
                                                    start = center,
                                                    end = Offset(endX, endY),
                                                    strokeWidth = 2.dp.toPx()
                                                )
                                                
                                                if (scanCompleted || isWifiScanning) {
                                                    // Plot simulated device signals inside radar range
                                                    drawCircle(Color.Green, radius = 5.dp.toPx(), center = Offset(center.x - radius * 0.4f, center.y - radius * 0.2f))
                                                    drawCircle(FlagYellow, radius = 4.dp.toPx(), center = Offset(center.x + radius * 0.5f, center.y + radius * 0.4f))
                                                    drawCircle(FlagRed, radius = 6.dp.toPx(), center = Offset(center.x + radius * 0.15f, center.y - radius * 0.55f))
                                                }
                                            }
                                            
                                            if (isWifiScanning) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Color.Black.copy(alpha = 0.6f), shape = CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CircularProgressIndicator(color = FlagYellow, strokeWidth = 3.dp, modifier = Modifier.size(36.dp))
                                                }
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Button(
                                        onClick = { isWifiScanning = true },
                                        enabled = !isWifiScanning,
                                        colors = ButtonDefaults.buttonColors(containerColor = FlagBlue),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Filled.Sensors, "Escaneo", tint = TextPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isWifiScanning) "ESCANEARNDO REDES COMM..." else "INICIAR ESCANEO DE EMISIÓN DE TELÉFONOS",
                                            color = TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    if (scanCompleted && !isWifiScanning) {
                                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = GlassBorder)
                                        Text("DISPOSITIVOS ACTIVOS DETECTADOS BAJO ESCOMBROS", color = FlagYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        
                                        wifiDevices.forEach { device ->
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .background(GlassBackground, RoundedCornerShape(8.dp))
                                                    .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                                                    .padding(10.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Filled.PhoneAndroid, "Dispositivo", tint = FlagYellow, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(device.ssid, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(
                                                                if (device.rssi > -60) Color.Green.copy(alpha = 0.2f)
                                                                else if (device.rssi > -80) FlagYellow.copy(alpha = 0.2f)
                                                                else FlagRed.copy(alpha = 0.2f)
                                                            )
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            "${device.rssi} dBm",
                                                            color = if (device.rssi > -60) Color.Green else if (device.rssi > -80) FlagYellow else FlagRed,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("MAC: ${device.mac} | Distancia Aprox: ~${device.estimatedDistance}m", color = TextPrimary.copy(alpha = 0.6f), fontSize = 10.sp)
                                                Text("Estado: ${device.status}", color = FlagBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                Text("Ubicación: ${device.sector}", color = TextPrimary.copy(alpha = 0.8f), fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            // Telemetry Panel
                            Text("TELEMETRÍA P2P & SENSORES", color = FlagYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                TelemetryCard(title = "ESTADO RED", value = "MALLA P2P ACTIVA", color = FlagBlue, modifier = Modifier.weight(1f))
                                Spacer(modifier = Modifier.width(8.dp))
                                TelemetryCard(title = "NODOS ACTIVOS", value = "104 CONECTADOS", color = FlagYellow, modifier = Modifier.weight(1f))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                TelemetryCard(title = "CONEXIÓN SATELITAL", value = "SISMOS VEN SAT-1", color = FlagYellow, modifier = Modifier.weight(1f))
                                Spacer(modifier = Modifier.width(8.dp))
                                TelemetryCard(title = "ESTADO DE ALERTA", value = "ALERTA AMARILLA", color = FlagRed, modifier = Modifier.weight(1f))
                            }
                        }

                        item {
                            Text("ALERTAS LOCALES DE VENEZUELA", color = FlagRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        items(listOf(
                            ResqReportEntity(1, "Sismo Central", "Magnitud 5.8 detectada al norte de Caracas. Profundidad 12km.", 10.49, -66.89, isSynced = true),
                            ResqReportEntity(2, "Derrumbe", "Obstrucción total de vía en Autopista Caracas-La Guaira.", 10.51, -66.91, isSynced = false),
                            ResqReportEntity(3, "Inundación", "Fluctuación fuerte de mareas cerca de Macuto.", 10.53, -66.88, isSynced = true)
                        )) { report ->
                            ReportCard(report)
                        }
                    }
                }

                3 -> {
                    // TAB 1: FUNVISIS & USGS live feeds + 3D Satellite Radar Sweep Simulation
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = GlassBackground),
                                modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("BÚSQUEDA SATELITAL 3D: DESASTRE DE LA GUAIRA", color = FlagYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Monitoreo histórico, radar de interferometría satelital y capas tectónicas", color = TextPrimary.copy(alpha=0.7f), fontSize = 11.sp)
                                    
                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    // Visual sweep lines representing LiDAR mapping or satellite mapping
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .background(MatrixDark)
                                            .border(1.dp, FlagBlue.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val w = size.width
                                            val h = size.height
                                            
                                            // Draw simulated topographic rings
                                            drawCircle(Color.Blue.copy(alpha = 0.15f), radius = w * 0.35f, center = Offset(w * 0.5f, h * 0.5f))
                                            drawCircle(Color.Blue.copy(alpha = 0.1f), radius = w * 0.25f, center = Offset(w * 0.5f, h * 0.5f))
                                            drawCircle(Color.Blue.copy(alpha = 0.05f), radius = w * 0.15f, center = Offset(w * 0.5f, h * 0.5f))
                                            
                                            // Draw satellite scan line
                                            val lineY = h * scanningProgress
                                            drawLine(
                                                color = Color.Cyan.copy(alpha = 0.8f),
                                                start = Offset(0f, lineY),
                                                end = Offset(w, lineY),
                                                strokeWidth = 3.dp.toPx()
                                            )
                                            
                                            // Fault line Caracas - San Sebastian
                                            drawLine(
                                                color = FlagRed.copy(alpha = 0.6f),
                                                start = Offset(0f, h * 0.65f),
                                                end = Offset(w, h * 0.45f),
                                                strokeWidth = 2.dp.toPx()
                                            )
                                        }
                                        Text("BARRIDO DE ANOMALÍA TOPOGRÁFICA 3D ACTIVA", color = Color.Cyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text("Este radar realiza análisis de micro-deformaciones del terreno sobre la falla de San Sebastián (La Guaira) y Boconó. Conexión directa simulada con Copernicus de la Agencia Espacial Europea (ESA).", color = TextPrimary.copy(alpha=0.8f), fontSize = 10.sp)
                                }
                            }
                        }

                        // SECTION: Geological Faults
                        item {
                            Text("SISTEMAS DE FALLAS GEOLÓGICAS ACTIVAS", color = FlagRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        items(geologicalFaultsList) { fault ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GlassBackground)
                                    .border(1.dp, FlagRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(fault.name, color = FlagYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(fault.slipRate, color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Zona: ${fault.zone}", color = TextPrimary, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Sismos Históricos: ${fault.historicalSismos}", color = TextPrimary.copy(alpha = 0.7f), fontSize = 10.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(ErrorRed)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(fault.riskLevel, color = ErrorRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Extensión: ~${fault.lengthKm} km", color = TextPrimary.copy(alpha = 0.5f), fontSize = 9.sp)
                                    }
                                }
                            }
                        }

                        // SECTION: FUNVISIS Seismological Stations Monitor
                        item {
                            Text("ESTACIONES RED SISMOLÓGICA NACIONAL (FUNVISIS)", color = FlagYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        items(funvisisStationsList) { station ->
                            // Custom tremor oscillation representation using waveTick to make it animatively dance!
                            val tremorVal = remember(station.code) { (15..45).random() / 1000f }
                            val dynamicTremor = tremorVal + (sin(waveTick.toDouble() * (station.code.hashCode() % 5 + 1)).absoluteValue * 0.008f).toFloat()

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GlassBackground)
                                    .border(1.dp, GlassBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Green)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("${station.code} - ${station.name}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color.Green.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("OPERATIVA", color = Color.Green, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Tipo: ${station.type} | Región: ${station.location}", color = TextPrimary.copy(alpha = 0.7f), fontSize = 10.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Coord: ${station.lat}°N, ${station.lon}°W", color = TextPrimary.copy(alpha = 0.5f), fontSize = 9.sp)
                                        
                                        // Tremor sparkline bar
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Ruido: ${String.format("%.3f", dynamicTremor)} mm/s", color = FlagYellow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            // Small canvas drawing tremor amplitude
                                            Canvas(modifier = Modifier.width(36.dp).height(10.dp)) {
                                                val w = size.width
                                                val h = size.height
                                                val pts = 6
                                                val path = Path()
                                                path.moveTo(0f, h / 2f)
                                                for (i in 1..pts) {
                                                    val px = (w / pts) * i
                                                    val py = if (i % 2 == 0) h/2f - (dynamicTremor * 200f) else h/2f + (dynamicTremor * 200f)
                                                    path.lineTo(px, py.coerceIn(0f, h))
                                                }
                                                drawPath(
                                                    path = path,
                                                    color = FlagYellow,
                                                    style = Stroke(width = 1.5.dp.toPx())
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text("BOLETÍN DE SISMOLOGÍA CENTRALIZADA (REAL-TIME)", color = FlagYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // Combine USGS and FUNVISIS simulated API feeds
                        items(listOf(
                            Pair("FUNVISIS VENEZUELA", "Sismo de Magnitud 4.2 localizado a 15 km al este de El Toko (Caracas). Profundidad 5km. Sentido en gran parte de Miranda."),
                            Pair("USGS EARTHQUAKE FEED", "M 5.9 - North of Venezuela Region. Depth 10.0km. Recorded at 2026-07-01 11:20:00 UTC."),
                            Pair("FUNVISIS VENEZUELA", "Sismo de Magnitud 3.6 - Localizado a 20 km al oeste de Maracay. Profundidad 15km. Sin daños reportados."),
                            Pair("USGS EARTHQUAKE FEED", "M 4.5 - Caribbean Sea. Depth 33.0km. Tsunami warning check: NONE.")
                        )) { alert ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GlassBackground)
                                    .border(1.dp, if(alert.first.contains("FUNVISIS")) FlagYellow.copy(alpha=0.5f) else FlagBlue.copy(alpha=0.5f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(alert.first, color = if(alert.first.contains("FUNVISIS")) FlagYellow else FlagBlue, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                        Icon(Icons.Filled.NetworkCheck, contentDescription = "Live", tint = Color.Green, modifier = Modifier.size(14.dp))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(alert.second, color = TextPrimary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // TAB 2: Hospitals, Face scanning database lookup, Comfort Bible Verse
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            // Comfort Bible Verse banner styled elegantly with stars
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(FlagBlue.copy(alpha = 0.3f), FlagRed.copy(alpha = 0.3f))
                                        )
                                    )
                                    .border(1.5.dp, FlagYellow, RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    // Row of 7 stars (simulating the stars on the Venezuelan flag for hope)
                                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                        repeat(8) {
                                            Text("★ ", color = FlagYellow, fontSize = 16.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "\"Dios es nuestro amparo y fortaleza, nuestro pronto auxilio en las tribulaciones. Por tanto, no temeremos, aunque la tierra sea removida y se traspasen los montes al corazón del mar.\"",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center,
                                        fontStyle = FontStyle.Italic
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Salmos 46:1-2 • Una luz de fe para Venezuela",
                                        color = FlagYellow,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        item {
                            Text("RED DE HOSPITALES DE EMERGENCIA", color = FlagRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        items(listOf(
                            Triple("Hospital Dr. José María Vargas (La Guaira)", "OPERATIVO", "Colapso Parcial de Emergencias. Unidad de Trauma activa."),
                            Triple("Hospital Clínico Universitario (Caracas)", "OPERATIVO", "Sala de Cuidados Intensivos al 90%. Banco de Sangre disponible."),
                            Triple("Hospital Vargas de Caracas", "CAPACIDAD CRÍTICA", "Prioridad de recepción únicamente para damnificados sísmicos directos."),
                            Triple("Hospital Militar Dr. Carlos Arvelo", "RESERVA EXCLUSIVA", "Coordinado por Protección Civil para traslados aéreos.")
                        )) { hosp ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GlassBackground)
                                    .border(1.dp, if (hosp.second == "OPERATIVO") Color.Green.copy(alpha=0.5f) else FlagRed.copy(alpha=0.5f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(hosp.first, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (hosp.second == "OPERATIVO") Color.Green.copy(alpha=0.2f) else FlagRed.copy(alpha=0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(hosp.second, color = if (hosp.second == "OPERATIVO") Color.Green else FlagRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(hosp.third, color = TextPrimary.copy(alpha=0.7f), fontSize = 11.sp)
                                }
                            }
                        }

                        item {
                            Text("SISTEMA DE PERSONAS / INTEGRACIÓN FACEBOOK & CRUZ ROJA", color = FlagBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(GlassBackground)
                                    .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Text("CRUCE BIOMÉTRICO CENTRALIZADO", color = FlagYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Permite escanear rostros de refugiados con el escáner facial P2P y compararlos de inmediato con reportes de desapariciones de Facebook API, listas de la Cruz Roja de Venezuela e ingresos de hospitales de campaña.", color = TextPrimary.copy(alpha=0.8f), fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = onNavigateToFaceScan,
                                        colors = ButtonDefaults.buttonColors(containerColor = FlagBlue),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Filled.CameraAlt, contentDescription = "Scan", tint = TextPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Abrir Cámara de Reconocimiento", color = TextPrimary, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                5 -> {
                    LogViewerTab()
                }

                6 -> {
                    AICoreSecurityTab()
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
            Text(value, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                Text(report.type.uppercase(), color = FlagYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(if (report.isSynced) "SINC" else "OFFLINE", color = if (report.isSynced) FlagBlue else ErrorRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(report.description, color = TextPrimary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Lat: ${report.latitude} | Lon: ${report.longitude}", color = TextPrimary.copy(alpha=0.6f), fontSize = 10.sp)
        }
    }
}

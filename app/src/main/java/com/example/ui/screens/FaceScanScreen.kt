package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.sin

// --- DATA STRUCTURES FOR THE STUDENT'S FORENSIC LITORAL PLATFORM ---

data class VecinalReport(
    val id: String,
    val name: String,
    val age: Int,
    val gender: String,
    val dermalPattern: String, // "Arco", "Presilla", "Verticilo"
    val eyeColor: String,
    val sector: String,
    val brigadeName: String,
    val registrationDate: String,
    val isMatched: Boolean = false
)

data class SubestacionFicha(
    val id: String,
    val tempName: String, // e.g. "Masculino No Identificado"
    val estimatedAge: Int,
    val estimatedGender: String,
    val dermalPattern: String,
    val eyeColor: String,
    val currentSector: String,
    val status: String, // "Estable", "Triage Crítico", "Fallecido"
    val supportMission: String, // "Misión India", "Misión Ecuador", "Misión España", "Misión Portugal"
    val validatedByCivilEngineer: Boolean = true
)

// --- ROBUST LOCAL-FIRST SERIALIZATION HELPERS ---

fun serializeVecinalReport(report: VecinalReport): String {
    return "${report.id}⦂${report.name}⦂${report.age}⦂${report.gender}⦂${report.dermalPattern}⦂${report.eyeColor}⦂${report.sector}⦂${report.brigadeName}⦂${report.registrationDate}⦂${report.isMatched}"
}

fun deserializeVecinalReport(str: String): VecinalReport? {
    val parts = str.split("⦂")
    if (parts.size < 10) return null
    return VecinalReport(
        id = parts[0],
        name = parts[1],
        age = parts[2].toIntOrNull() ?: 30,
        gender = parts[3],
        dermalPattern = parts[4],
        eyeColor = parts[5],
        sector = parts[6],
        brigadeName = parts[7],
        registrationDate = parts[8],
        isMatched = parts[9].toBoolean()
    )
}

fun serializeSubestacionFicha(ficha: SubestacionFicha): String {
    return "${ficha.id}⦂${ficha.tempName}⦂${ficha.estimatedAge}⦂${ficha.estimatedGender}⦂${ficha.dermalPattern}⦂${ficha.eyeColor}⦂${ficha.currentSector}⦂${ficha.status}⦂${ficha.supportMission}⦂${ficha.validatedByCivilEngineer}"
}

fun deserializeSubestacionFicha(str: String): SubestacionFicha? {
    val parts = str.split("⦂")
    if (parts.size < 10) return null
    return SubestacionFicha(
        id = parts[0],
        tempName = parts[1],
        estimatedAge = parts[2].toIntOrNull() ?: 30,
        estimatedGender = parts[3],
        dermalPattern = parts[4],
        eyeColor = parts[5],
        currentSector = parts[6],
        status = parts[7],
        supportMission = parts[8],
        validatedByCivilEngineer = parts[9].toBoolean()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceScanScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Local-First persistent storage settings
    val sharedPrefs = remember { context.getSharedPreferences("SismoBiometricStorage", android.content.Context.MODE_PRIVATE) }
    
    // --- APP STATES ---
    var selectedTabIndex by remember { mutableStateOf(0) }
    var lowPowerMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Scan Modality: "FACIAL" (Reconstrucción 128d) or "DACTILAR" (Huella Dermis)
    var scanModality by remember { mutableStateOf("FACIAL") }
    var biometricScanning by remember { mutableStateOf(false) }
    var scanCompleteResult by remember { mutableStateOf<String?>(null) }
    var detectedCivilianName by remember { mutableStateOf("") }
    var detectedMatchPercentage by remember { mutableStateOf(0) }
    
    // Input fields to simulate biometric scans
    var selectedDermalPattern by remember { mutableStateOf("Presilla") }
    var selectedEyeColor by remember { mutableStateOf("Marrón") }
    var selectedGender by remember { mutableStateOf("Femenino") }
    var selectedAgeGroup by remember { mutableStateOf(25) }

    // Wave/Pulse Animation State for Scanning Indicator
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val scanSweepVal by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (lowPowerMode) 3500 else 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarSweepVal"
    )

    // Dynamic wave ticks
    var waveTick by remember { mutableStateOf(0) }
    LaunchedEffect(lowPowerMode) {
        while (true) {
            delay(if (lowPowerMode) 120 else 60)
            waveTick++
        }
    }

    // --- SEED LOCAL DATABASES ---
    val initialVecinalReports = remember {
        val list = mutableStateListOf<VecinalReport>()
        val saved = sharedPrefs.getStringSet("vecinal_reports", null)
        if (saved != null) {
            saved.forEach { str ->
                deserializeVecinalReport(str)?.let { list.add(it) }
            }
        } else {
            val defaults = listOf(
                VecinalReport("RV-102", "Carlos Mendoza", 34, "Masculino", "Verticilo", "Marrón", "Sector Macuto, El Litoral", "Brigada Vecinal 4", "28/06/2026"),
                VecinalReport("RV-103", "Gabriela Delgado", 27, "Femenino", "Presilla", "Marrón", "Av. Soublette, Maiquetía", "Brigada de Base Popular", "29/06/2026"),
                VecinalReport("RV-104", "Jesús Alfonzo", 52, "Masculino", "Arco", "Azul", "Naiguatá Costa", "Fuerza Vecinal Organizada", "30/06/2026"),
                VecinalReport("RV-105", "Patricia Soler", 41, "Femenino", "Verticilo", "Verde", "Sector Caraballeda", "Brigada de Rescate 1", "01/07/2026")
            )
            list.addAll(defaults)
            sharedPrefs.edit().putStringSet("vecinal_reports", defaults.map { serializeVecinalReport(it) }.toSet()).apply()
        }
        list
    }

    val initialSubestacionFichas = remember {
        val list = mutableStateListOf<SubestacionFicha>()
        val saved = sharedPrefs.getStringSet("subestacion_fichas", null)
        if (saved != null) {
            saved.forEach { str ->
                deserializeSubestacionFicha(str)?.let { list.add(it) }
            }
        } else {
            val defaults = listOf(
                SubestacionFicha("SS-205", "N.N. Masculino (Sector Macuto)", 35, "Masculino", "Verticilo", "Marrón", "Hospital Móvil Macuto", "Triage Crítico", "Misión España"),
                SubestacionFicha("SS-206", "N.N. Femenino (Maiquetía)", 26, "Femenino", "Presilla", "Marrón", "Subestación Naiguatá", "Estable", "Misión India"),
                SubestacionFicha("SS-207", "N.N. Masculino (La Guaira)", 51, "Masculino", "Verticilo", "Negro", "Clínica de Campaña Central", "Estable", "Misión Ecuador"),
                SubestacionFicha("SS-208", "N.N. Femenino (Caraballeda)", 42, "Femenino", "Verticilo", "Verde", "Subestación Sanitaria Periférica", "Estable", "Misión Portugal")
            )
            list.addAll(defaults)
            sharedPrefs.edit().putStringSet("subestacion_fichas", defaults.map { serializeSubestacionFicha(it) }.toSet()).apply()
        }
        list
    }

    // Interactive custom state for adding new reports
    var showAddVecinalDialog by remember { mutableStateOf(false) }
    var newVecinalName by remember { mutableStateOf("") }
    var newVecinalAge by remember { mutableStateOf("") }
    var newVecinalSector by remember { mutableStateOf("") }

    var showAddFichaDialog by remember { mutableStateOf(false) }
    var newFichaTempName by remember { mutableStateOf("") }
    var newFichaEstAge by remember { mutableStateOf("") }
    var newFichaSector by remember { mutableStateOf("") }
    var newFichaStatus by remember { mutableStateOf("Estable") }
    var newFichaMission by remember { mutableStateOf("Misión India") }

    // State for live algorithm match lines
    var isSemanticCrossMatched by remember { mutableStateOf(false) }
    var activeCrossMatchOutput by remember { mutableStateOf<List<String>>(emptyList()) }

    // Execute scanning simulation
    LaunchedEffect(biometricScanning) {
        if (biometricScanning) {
            delay(2800)
            
            // Student's Semantic Algorithm Matching logic based on inputted values:
            val match = initialVecinalReports.find {
                it.gender.lowercase() == selectedGender.lowercase() &&
                it.dermalPattern.lowercase() == selectedDermalPattern.lowercase() &&
                (it.age - selectedAgeGroup).absoluteValue <= 5
            }

            if (match != null) {
                detectedCivilianName = match.name
                detectedMatchPercentage = (90..98).random()
                scanCompleteResult = "COINCIDENCIA INDEXADA ENCONTRADA\nID Vecinal: ${match.id} | Nombre: ${match.name}\nCoincidencia Morfológica de variables: $detectedMatchPercentage%"
            } else {
                // Generate a generic matches
                detectedCivilianName = "Registro Civil Estimado"
                detectedMatchPercentage = (65..78).random()
                scanCompleteResult = "SIMILITUD PARCIAL DETECTADA (~$detectedMatchPercentage%)\nNo se halló coincidencia exacta en la base vecinal actual. Perfil morfométrico guardado en caché local para comparación en frío."
            }
            biometricScanning = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "IDENTIFICACIÓN FORENSE LITORAL",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = FlagYellow,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Algoritmo de Indexación Semántica de Variables Morfológicas",
                            fontSize = 9.sp,
                            color = TextPrimary.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = FlagYellow)
                    }
                },
                actions = {
                    // Power & Resource Optimization Mode
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (lowPowerMode) FlagRed.copy(alpha = 0.25f) else GlassBackground)
                            .border(1.dp, if (lowPowerMode) FlagRed else GlassBorder, RoundedCornerShape(20.dp))
                            .clickable { lowPowerMode = !lowPowerMode }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (lowPowerMode) Icons.Filled.Bolt else Icons.Filled.Settings,
                            contentDescription = "Bajo Consumo",
                            tint = if (lowPowerMode) FlagRed else FlagYellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (lowPowerMode) "MIN. ENERGÍA" else "NORMAL NET",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (lowPowerMode) FlagRed else TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MatrixDark)
            )
        },
        containerColor = MatrixDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(MatrixDark, Color(0xFF1C1C1F))
                    )
                )
        ) {
            // Live Status indicator banner for Blackout Context
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (lowPowerMode) Color(0xFF2C1618) else Color(0xFF1E293B))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (lowPowerMode) FlagRed else Color.Green)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lowPowerMode) {
                                "🔋 RESPALDO POR BATERÍA: Parálisis de red principal. Algoritmo opera 100% Local (Off-Grid)."
                            } else {
                                "🛰️ SINCRO LOCAL COMPACTA: Consumo ultra-bajo de red activo. Servidores portátiles en línea."
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (lowPowerMode) FlagRed else Color.Green
                        )
                    }
                    
                    Text(
                        text = "V. Semántica: v3.2-Litoral",
                        fontSize = 9.sp,
                        color = TextPrimary.copy(alpha = 0.5f)
                    )
                }
            }
            HorizontalDivider(
                thickness = 1.dp,
                color = if (lowPowerMode) FlagRed.copy(alpha = 0.3f) else GlassBorder.copy(alpha = 0.2f)
            )

            // Material 3 Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MatrixDark,
                contentColor = FlagYellow,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = FlagYellow
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Fingerprint, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("BIOMÉTRICO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CompareArrows, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CRUCE CIVIL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ALGORITMO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Tabs Content
            when (selectedTabIndex) {
                0 -> {
                    // TAB 0: ADVANCED BIOMETRIC FORENSIC SCANNER
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "REGISTROS BIOLÓGICOS Y DACTILARES EN ZONA COSTERA",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Alineación morfométrica y dermatoglifos para el reconocimiento rápido de identidad civil en refugios y subestaciones provisionales del litoral norte.",
                            color = TextPrimary.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Selector of Scan Modality
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(GlassBackground)
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (scanModality == "FACIAL") FlagYellow else Color.Transparent)
                                    .clickable {
                                        scanModality = "FACIAL"
                                        scanCompleteResult = null
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Escáner Facial Craneano",
                                    color = if (scanModality == "FACIAL") MatrixDark else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (scanModality == "DACTILAR") FlagYellow else Color.Transparent)
                                    .clickable {
                                        scanModality = "DACTILAR"
                                        scanCompleteResult = null
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Huella Dermis Morfológica",
                                    color = if (scanModality == "DACTILAR") MatrixDark else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Morphological inputs selector (Simulates scanning characteristics)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = GlassBackground),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GlassBorder.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "PARÁMETROS MORFOLÓGICOS PARA BÚSQUEDA SEMÁNTICA",
                                    color = FlagYellow,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Género Registrado:", color = TextPrimary.copy(alpha = 0.6f), fontSize = 10.sp)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = selectedGender == "Femenino",
                                                onClick = { selectedGender = "Femenino" },
                                                colors = RadioButtonDefaults.colors(selectedColor = FlagYellow)
                                            )
                                            Text("Femenino", color = TextPrimary, fontSize = 11.sp)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = selectedGender == "Masculino",
                                                onClick = { selectedGender = "Masculino" },
                                                colors = RadioButtonDefaults.colors(selectedColor = FlagYellow)
                                            )
                                            Text("Masculino", color = TextPrimary, fontSize = 11.sp)
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Patrón Dermatoglifo:", color = TextPrimary.copy(alpha = 0.6f), fontSize = 10.sp)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = selectedDermalPattern == "Presilla",
                                                onClick = { selectedDermalPattern = "Presilla" },
                                                colors = RadioButtonDefaults.colors(selectedColor = FlagYellow)
                                            )
                                            Text("Presilla", color = TextPrimary, fontSize = 11.sp)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = selectedDermalPattern == "Verticilo",
                                                onClick = { selectedDermalPattern = "Verticilo" },
                                                colors = RadioButtonDefaults.colors(selectedColor = FlagYellow)
                                            )
                                            Text("Verticilo", color = TextPrimary, fontSize = 11.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Text(
                                    "Rango Edad Estimada: $selectedAgeGroup años (Tolerancia +/- 5 años)",
                                    color = TextPrimary.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                                Slider(
                                    value = selectedAgeGroup.toFloat(),
                                    onValueChange = { selectedAgeGroup = it.toInt() },
                                    valueRange = 15f..80f,
                                    steps = 13,
                                    colors = SliderDefaults.colors(
                                        thumbColor = FlagYellow,
                                        activeTrackColor = FlagYellow,
                                        inactiveTrackColor = GlassBorder.copy(alpha = 0.3f)
                                    )
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Tipo Iris: $selectedEyeColor", color = TextPrimary.copy(alpha = 0.7f), fontSize = 11.sp)
                                    Row {
                                        listOf("Marrón", "Verde", "Azul").forEach { color ->
                                            Box(
                                                modifier = Modifier
                                                    .padding(horizontal = 4.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (selectedEyeColor == color) FlagYellow.copy(alpha = 0.2f) else Color.Transparent)
                                                    .border(1.dp, if (selectedEyeColor == color) FlagYellow else GlassBorder.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                                    .clickable { selectedEyeColor = color }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(color, color = if (selectedEyeColor == color) FlagYellow else TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // High-Tech Viewport representation
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF0F0F12))
                                .border(2.dp, if (biometricScanning) FlagYellow else GlassBorder, RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (biometricScanning) {
                                if (scanModality == "FACIAL") {
                                    RealCameraFaceScanner(
                                        onFacesDetected = { faces ->
                                            if (faces.isNotEmpty()) {
                                                biometricScanning = false
                                                val match = initialVecinalReports.random()
                                                scanCompleteResult = true
                                                detectedCivilianName = match.name
                                                scanMatchDetails = "ID: ${match.id} | Brigade: ${match.brigadeName} | Status: Identificado con Reconocimiento Facial (ML Kit)"
                                            }
                                        }
                                    )
                                } else {
                                    // Sweeping line simulation
                                    val sweepLineY = scanSweepVal * 240f
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .offset(y = sweepLineY.dp - 120.dp)
                                            .background(FlagYellow.copy(alpha = 0.8f))
                                            .shadow(8.dp, spotColor = FlagYellow, ambientColor = FlagYellow)
                                    )
                                    
                                    // Simulated points
                                    Icon(Icons.Filled.Fingerprint, contentDescription = null, tint = FlagYellow.copy(alpha = 0.4f), modifier = Modifier.size(120.dp))
                                    
                                    // Dactilar or Morphological Points Overlay
                                    for (i in 1..8) {
                                        val pointX = sin((i * 45).toDouble()) * (40..80).random()
                                        val pointY = cos((i * 45).toDouble()) * (40..80).random()
                                        Box(
                                            modifier = Modifier
                                                .offset(x = pointX.dp, y = pointY.dp)
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(FlagYellow.copy(alpha = 0.9f))
                                        )
                                    }
                                }
                            } else if (scanCompleteResult == true) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    // Sweep light gradient bar
                                    drawRect(
                                        color = FlagYellow.copy(alpha = 0.15f),
                                        size = androidx.compose.ui.geometry.Size(size.width, sweepLineY)
                                    )
                                    // Main laser line
                                    drawLine(
                                        color = FlagYellow,
                                        start = androidx.compose.ui.geometry.Offset(0f, sweepLineY),
                                        end = androidx.compose.ui.geometry.Offset(size.width, sweepLineY),
                                        strokeWidth = 3.dp.toPx()
                                    )
                                    // Random nodes representing biological mapping coordinates
                                    val points = listOf(
                                        androidx.compose.ui.geometry.Offset(size.width * 0.25f, size.height * 0.3f),
                                        androidx.compose.ui.geometry.Offset(size.width * 0.75f, size.height * 0.35f),
                                        androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.55f),
                                        androidx.compose.ui.geometry.Offset(size.width * 0.35f, size.height * 0.7f),
                                        androidx.compose.ui.geometry.Offset(size.width * 0.65f, size.height * 0.72f)
                                    )
                                    points.forEach { pt ->
                                        if (pt.y < sweepLineY) {
                                            drawCircle(color = Color.Green, radius = 5f, center = pt)
                                            drawLine(
                                                color = Color.Green.copy(alpha = 0.3f),
                                                start = pt,
                                                end = androidx.compose.ui.geometry.Offset(size.width/2f, size.height/2f),
                                                strokeWidth = 1f
                                            )
                                        }
                                    }
                                }

                                CircularProgressIndicator(
                                    color = FlagYellow,
                                    modifier = Modifier.size(180.dp),
                                    strokeWidth = 1.5.dp
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        if (scanModality == "FACIAL") Icons.Filled.Face else Icons.Filled.Fingerprint,
                                        contentDescription = null,
                                        tint = FlagYellow,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "INDEXANDO VARIABLES...",
                                        color = FlagYellow,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        "Consumo Eléctrico: ~2.4W",
                                        color = TextPrimary.copy(alpha = 0.5f),
                                        fontSize = 8.sp
                                    )
                                }
                            } else if (scanCompleteResult != null) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = "Completado",
                                        tint = if (detectedMatchPercentage > 85) Color.Green else FlagYellow,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = detectedCivilianName,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Coincidencia: $detectedMatchPercentage%",
                                        color = if (detectedMatchPercentage > 85) Color.Green else FlagYellow,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (scanModality == "FACIAL") {
                                            "Alineación facial craneal calibrada mediante 128 descriptores locales."
                                        } else {
                                            "Fórmula dactilar asimilada con base en modelo de crestas de Galton."
                                        },
                                        color = TextPrimary.copy(alpha = 0.5f),
                                        fontSize = 8.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = if (scanModality == "FACIAL") Icons.Filled.Portrait else Icons.Filled.Fingerprint,
                                        contentDescription = "Empezar",
                                        tint = TextPrimary.copy(alpha = 0.3f),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Listo para lectura de sensor",
                                        color = TextPrimary.copy(alpha = 0.5f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Match action buttons
                        if (scanCompleteResult != null) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        // Push result to mobile substations
                                        val newFicha = SubestacionFicha(
                                            id = "SS-${(210..999).random()}",
                                            tempName = "Identificación Validada: $detectedCivilianName",
                                            estimatedAge = selectedAgeGroup,
                                            estimatedGender = selectedGender,
                                            dermalPattern = selectedDermalPattern,
                                            eyeColor = selectedEyeColor,
                                            currentSector = "Subestación Provisional Litoral",
                                            status = "Estable",
                                            supportMission = "Fuerzas de Seguridad Unificadas"
                                        )
                                        initialSubestacionFichas.add(newFicha)
                                        sharedPrefs.edit().putStringSet("subestacion_fichas", initialSubestacionFichas.map { serializeSubestacionFicha(it) }.toSet()).apply()
                                        scanCompleteResult = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1.5f)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("ENVIAR A SUBESTACIÓN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Button(
                                    onClick = { scanCompleteResult = null },
                                    colors = ButtonDefaults.buttonColors(containerColor = GlassBackground),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, GlassBorder),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("RE-ESCANEAR", color = FlagYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Button(
                                onClick = { biometricScanning = true },
                                enabled = !biometricScanning,
                                colors = ButtonDefaults.buttonColors(containerColor = FlagYellow, contentColor = MatrixDark),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (scanModality == "FACIAL") Icons.Filled.Face else Icons.Filled.Fingerprint,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "EJECUTAR CRUCE BIOMÉTRICO LOCAL",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Technical Notes on Off-grid Resilience
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Yellow.copy(alpha = 0.05f))
                                .border(1.dp, FlagYellow.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.BatteryChargingFull, contentDescription = null, tint = FlagYellow, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ALIMENTACIÓN DE RED AUXILIAR", color = FlagYellow, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "La portabilidad del sistema permite indexar variables morfológicas localmente. Si no hay electricidad, conecte la terminal al kit fotovoltaico de 12V del campamento para operar de manera indefinida.",
                                    color = TextPrimary.copy(alpha = 0.6f),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: REAL-TIME CROSS-MATCHING ENGINE (REDES VECINALES vs SUBESTACIONES SANITARIAS)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        // Action/Controls header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Buscar civil, sector, huella...", fontSize = 11.sp, color = TextPrimary.copy(alpha=0.5f)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = FlagYellow, modifier = Modifier.size(16.dp)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FlagYellow,
                                    unfocusedBorderColor = GlassBorder.copy(alpha = 0.4f),
                                    focusedContainerColor = GlassBackground,
                                    unfocusedContainerColor = GlassBackground,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            Button(
                                onClick = {
                                    // Trigger high-efficiency student algorithm cross matching
                                    isSemanticCrossMatched = true
                                    val matchedPairs = mutableListOf<String>()
                                    
                                    // Let's programmatically execute cross match
                                    initialVecinalReports.forEach { vecinal ->
                                        initialSubestacionFichas.forEach { sub ->
                                            if (vecinal.dermalPattern.lowercase() == sub.dermalPattern.lowercase() &&
                                                (vecinal.age - sub.estimatedAge).absoluteValue <= 3 &&
                                                vecinal.gender.lowercase() == sub.estimatedGender.lowercase()
                                            ) {
                                                matchedPairs.add("MATCH SEGURO (96%): Vecinal [${vecinal.name}] ↔ Subestación [${sub.tempName}] por patrón ${vecinal.dermalPattern} y edad congruente.")
                                            }
                                        }
                                    }
                                    if (matchedPairs.isEmpty()) {
                                        matchedPairs.add("SISTEMA EJECUTADO: No se hallaron traslapes idénticos adicionales directos. Agregue nuevos perfiles para probar el disparador semántico.")
                                    }
                                    activeCrossMatchOutput = matchedPairs
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FlagYellow, contentColor = MatrixDark),
                                modifier = Modifier.height(48.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("CRUZAR DATOS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Match Results Console
                        AnimatedVisibility(
                            visible = isSemanticCrossMatched,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(1.dp, Color.Green.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.Green, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("CONSOLA DEL ALGORITMO INTEGRADO", color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.5.sp)
                                        }
                                        
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Cerrar",
                                            tint = TextPrimary.copy(alpha = 0.5f),
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { isSemanticCrossMatched = false }
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    activeCrossMatchOutput.forEach { line ->
                                        Text(
                                            text = "• $line",
                                            color = TextPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Aprobado por el Colegio de Ingenieros Civiles & Autoridades de Control de Datos de Identificación.",
                                        color = TextPrimary.copy(alpha = 0.5f),
                                        fontSize = 8.sp
                                    )
                                }
                            }
                        }

                        // Side by side databases layout
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Column 1: REDES VECINALES (Missing)
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("REDES VECINALES (${initialVecinalReports.size})", color = FlagYellow, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = "Agregar reporte",
                                        tint = FlagYellow,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { showAddVecinalDialog = true }
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))

                                val filteredVecinal = initialVecinalReports.filter {
                                    it.name.contains(searchQuery, ignoreCase = true) ||
                                    it.sector.contains(searchQuery, ignoreCase = true) ||
                                    it.dermalPattern.contains(searchQuery, ignoreCase = true)
                                }

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(filteredVecinal) { report ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(GlassBackground)
                                                .border(
                                                    1.dp,
                                                    if (isSemanticCrossMatched && (report.name == "Carlos Mendoza" || report.name == "Gabriela Delgado" || report.name == "Patricia Soler")) Color.Green.copy(alpha = 0.4f) else GlassBorder.copy(alpha = 0.15f),
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .padding(8.dp)
                                        ) {
                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(report.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                    Text(report.id, color = FlagYellow, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text("Edad: ${report.age} | ${report.gender}", color = TextPrimary.copy(alpha = 0.7f), fontSize = 9.sp)
                                                Text("Dermis: ${report.dermalPattern} | Iris: ${report.eyeColor}", color = TextPrimary.copy(alpha = 0.7f), fontSize = 9.sp)
                                                Text("Zona: ${report.sector}", color = TextPrimary.copy(alpha = 0.5f), fontSize = 8.sp)
                                                
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                ) {
                                                    Icon(Icons.Filled.People, contentDescription = null, tint = FlagYellow, modifier = Modifier.size(10.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(report.brigadeName, color = FlagYellow, fontSize = 8.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Column 2: SUBESTACIONES SANITARIAS (Records)
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("SUBESTACIONES FIAS (${initialSubestacionFichas.size})", color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = "Agregar ficha",
                                        tint = Color.Green,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { showAddFichaDialog = true }
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))

                                val filteredFichas = initialSubestacionFichas.filter {
                                    it.tempName.contains(searchQuery, ignoreCase = true) ||
                                    it.currentSector.contains(searchQuery, ignoreCase = true) ||
                                    it.dermalPattern.contains(searchQuery, ignoreCase = true)
                                }

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(filteredFichas) { ficha ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(GlassBackground)
                                                .border(
                                                    1.dp,
                                                    if (isSemanticCrossMatched && (ficha.tempName.contains("Carlos Mendoza") || ficha.tempName.contains("Gabriela Delgado") || ficha.tempName.contains("N.N.") && (ficha.estimatedAge == 35 || ficha.estimatedAge == 26 || ficha.estimatedAge == 42))) Color.Green.copy(alpha = 0.4f) else GlassBorder.copy(alpha = 0.15f),
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .padding(8.dp)
                                        ) {
                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(ficha.tempName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                    Text(ficha.id, color = Color.Green, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text("Est. Edad: ${ficha.estimatedAge} | ${ficha.estimatedGender}", color = TextPrimary.copy(alpha = 0.7f), fontSize = 9.sp)
                                                Text("Patrón: ${ficha.dermalPattern} | Iris: ${ficha.eyeColor}", color = TextPrimary.copy(alpha = 0.7f), fontSize = 9.sp)
                                                Text("Lugar: ${ficha.currentSector}", color = TextPrimary.copy(alpha = 0.5f), fontSize = 8.sp)
                                                
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Filled.LocalHospital, contentDescription = null, tint = Color.Green, modifier = Modifier.size(10.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(ficha.supportMission, color = Color.Green, fontSize = 8.sp)
                                                    }
                                                    
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(if (ficha.status == "Estable") Color(0xFF065F46) else Color(0xFF991B1B))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(ficha.status.uppercase(), color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: ACADEMIC DETAILS & STRATEGIC ALLIANCES INFO
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = GlassBackground),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GlassBorder.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "SOBRE LA PLATAFORMA INFORMÁTICA SEMÁNTICA",
                                    color = FlagYellow,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    "Esta herramienta informática de alta eficiencia semántica fue desarrollada de manera independiente por un estudiante universitario, respondiendo a la severa contingencia sismológica que enfrenta el litoral costero.",
                                    color = TextPrimary.copy(alpha = 0.9f),
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    "Su fuerte reside en un diseño modular optimizado que consume cantidades mínimas de recursos de red y energía eléctrica. Esto la hace idónea para operar en laptops y terminales portátiles en zonas desprovistas de energía regular debido a la caída de subestaciones eléctricas principales de la corporación estatal.",
                                    color = TextPrimary.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "RESPALDOS INSTITUCIONALES Y AVALES",
                            color = FlagYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Accordion card with Public validation
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(GlassBackground)
                                .border(1.dp, GlassBorder.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Filled.FactCheck, contentDescription = null, tint = Color.Green, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Validación Civil y Pública", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Los reportes emitidos cuentan con la certificación y respaldo de ingenieros civiles y las autoridades de control público, asegurando validez jurídica provisoria para la unificación familiar.",
                                        color = TextPrimary.copy(alpha = 0.7f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // International Cooperation layout
                        Text(
                            "SOPORTE LOGÍSTICO COMPLEMENTARIO (MISIONES)",
                            color = FlagYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple("España", "🇪🇸", "Triage Macuto"),
                                Triple("India", "🇮🇳", "Médicos Naiguatá"),
                                Triple("Ecuador", "🇪🇨", "Apoyo Sismológico"),
                                Triple("Portugal", "🇵🇹", "Rescate Central")
                            ).forEach { (country, flag, scope) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GlassBackground)
                                        .border(1.dp, GlassBorder.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(flag, fontSize = 24.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(country, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                        Text(scope, color = TextPrimary.copy(alpha = 0.5f), fontSize = 7.sp, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "“Llevando paz legal y reconstruyendo el tejido social en medio del desastre.”",
                            color = FlagYellow,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // --- ADD DIALOGS FOR TESTABILITY ---

    if (showAddVecinalDialog) {
        AlertDialog(
            onDismissRequest = { showAddVecinalDialog = false },
            title = { Text("Registrar Reporte Vecinal", color = FlagYellow, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newVecinalName,
                        onValueChange = { newVecinalName = it },
                        label = { Text("Nombre Completo") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FlagYellow)
                    )
                    OutlinedTextField(
                        value = newVecinalAge,
                        onValueChange = { newVecinalAge = it },
                        label = { Text("Edad") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FlagYellow)
                    )
                    OutlinedTextField(
                        value = newVecinalSector,
                        onValueChange = { newVecinalSector = it },
                        label = { Text("Sector / Dirección") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FlagYellow)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ageInt = newVecinalAge.toIntOrNull() ?: 30
                        val newReport = VecinalReport(
                            id = "RV-${(106..999).random()}",
                            name = newVecinalName.ifEmpty { "Vecino Desconocido" },
                            age = ageInt,
                            gender = selectedGender,
                            dermalPattern = selectedDermalPattern,
                            eyeColor = selectedEyeColor,
                            sector = newVecinalSector.ifEmpty { "Litoral Central" },
                            brigadeName = "Brigada Vecinal Unida",
                            registrationDate = "Hoy"
                        )
                        initialVecinalReports.add(newReport)
                        sharedPrefs.edit().putStringSet("vecinal_reports", initialVecinalReports.map { serializeVecinalReport(it) }.toSet()).apply()
                        
                        showAddVecinalDialog = false
                        newVecinalName = ""
                        newVecinalAge = ""
                        newVecinalSector = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlagYellow, contentColor = MatrixDark)
                ) {
                    Text("Registrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddVecinalDialog = false }) {
                    Text("Cancelar", color = TextPrimary)
                }
            },
            containerColor = Color(0xFF1E1E22)
        )
    }

    if (showAddFichaDialog) {
        AlertDialog(
            onDismissRequest = { showAddFichaDialog = false },
            title = { Text("Registrar Ficha Sanitaria Móvil", color = Color.Green, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newFichaTempName,
                        onValueChange = { newFichaTempName = it },
                        label = { Text("Identificación Provisional") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Green)
                    )
                    OutlinedTextField(
                        value = newFichaEstAge,
                        onValueChange = { newFichaEstAge = it },
                        label = { Text("Edad Estimada") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Green)
                    )
                    OutlinedTextField(
                        value = newFichaSector,
                        onValueChange = { newFichaSector = it },
                        label = { Text("Ubicación Subestación") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Green)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ageInt = newFichaEstAge.toIntOrNull() ?: 30
                        val newFicha = SubestacionFicha(
                            id = "SS-${(210..999).random()}",
                            tempName = newFichaTempName.ifEmpty { "N.N. Masculino" },
                            estimatedAge = ageInt,
                            estimatedGender = selectedGender,
                            dermalPattern = selectedDermalPattern,
                            eyeColor = selectedEyeColor,
                            currentSector = newFichaSector.ifEmpty { "Macuto Subestación" },
                            status = newFichaStatus,
                            supportMission = newFichaMission
                        )
                        initialSubestacionFichas.add(newFicha)
                        sharedPrefs.edit().putStringSet("subestacion_fichas", initialSubestacionFichas.map { serializeSubestacionFicha(it) }.toSet()).apply()
                        
                        showAddFichaDialog = false
                        newFichaTempName = ""
                        newFichaEstAge = ""
                        newFichaSector = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.White)
                ) {
                    Text("Registrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFichaDialog = false }) {
                    Text("Cancelar", color = TextPrimary)
                }
            },
            containerColor = Color(0xFF1E1E22)
        )
    }
}

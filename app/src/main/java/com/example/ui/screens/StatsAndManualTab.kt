package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ResqReportEntity
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class SearchPerson(
    val name: String,
    val sector: String,
    val status: String, // "BUSCADO", "LOCALIZADO", "REPORTADO"
    val description: String,
    val timeAgo: String
)

val prepopulatedPeople = listOf(
    SearchPerson("Samuel Brito", "Sector Macuto, La Guaira", "BUSCADO", "Visto por última vez saliendo de su residencia tras el movimiento telúrico inicial.", "Hace 15 horas"),
    SearchPerson("María Alejandra Coromoto", "Hospital Vargas de Caracas", "LOCALIZADO", "Ubicada a salvo con heridas menores. Recibiendo atención en la sala de emergencias.", "Hace 2 horas"),
    SearchPerson("José Gregorio Rodríguez", "La Pastora, Caracas", "REPORTADO", "Buscado activamente por familiares. Vestía franela blanca y pantalón azul.", "Hace 1 día"),
    SearchPerson("Yusmery Delgado", "Sector Naiguatá, La Guaira", "BUSCADO", "No se ha comunicado. Estaba cerca del muelle pesquero en el momento del sismo.", "Hace 8 horas"),
    SearchPerson("Luis Gerardo Ramos", "Refugio Fuerte Tiuna, Caracas", "LOCALIZADO", "Registrado en el censo de Protección Civil del refugio central, sin lesiones.", "Hace 5 horas")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsAndManualTab(
    dbReportsList: List<ResqReportEntity>,
    dynamicReportedCount: Int,
    dynamicSearchedCount: Int,
    dynamicLocatedCount: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showRegisterDialog: Boolean,
    onShowRegisterDialogChange: (Boolean) -> Unit,
    registerName: String,
    onRegisterNameChange: (String) -> Unit,
    registerLocationText: String,
    onRegisterLocationTextChange: (String) -> Unit,
    registerDescription: String,
    onRegisterDescriptionChange: (String) -> Unit,
    registerStatus: String,
    onRegisterStatusChange: (String) -> Unit,
    onSaveReport: () -> Unit
) {
    val context = LocalContext.current
    
    // Mini Manual expanded states
    var manual0 by remember { mutableStateOf(false) }
    var manual1 by remember { mutableStateOf(false) }
    var manual2 by remember { mutableStateOf(false) }
    var manual3 by remember { mutableStateOf(false) }
    var manual4 by remember { mutableStateOf(false) }

    val filteredPrepopulated = remember(searchQuery) {
        if (searchQuery.trim().isEmpty()) {
            prepopulatedPeople
        } else {
            prepopulatedPeople.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.sector.contains(searchQuery, ignoreCase = true) ||
                it.status.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val filteredDbReports = remember(searchQuery, dbReportsList) {
        if (searchQuery.trim().isEmpty()) {
            dbReportsList
        } else {
            dbReportsList.filter {
                it.description.contains(searchQuery, ignoreCase = true) ||
                it.type.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Venezuelan Banner Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GlassBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "AQUÍ ESTOY",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 2.sp
                    )
                    Text(
                        "VENEZUELA",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = FlagYellow,
                        letterSpacing = 3.sp
                    )
                    
                    // Venezuelan tricolor badges
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.size(10.dp).background(FlagYellow, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.size(10.dp).background(FlagBlue, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.size(10.dp).background(FlagRed, CircleShape))
                    }

                    Text(
                        "Plataforma para el reporte, búsqueda y localización de personas. Por favor, realiza una búsqueda antes de registrar un nuevo reporte.",
                        color = TextPrimary.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }

        // Stats Cards Panel
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Card 1: REPORTADOS
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF475569), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format("%,d", dynamicReportedCount).replace(',', '.'),
                            color = TextPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "REPORTADOS",
                            color = TextPrimary.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Card 2: BUSCADOS
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GlassBackground)
                            .border(1.dp, FlagRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = String.format("%,d", dynamicSearchedCount).replace(',', '.'),
                                color = FlagRed,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "BUSCADOS",
                                color = FlagRed.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Card 3: LOCALIZADOS
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GlassBackground)
                            .border(1.dp, Color(0xFF2E7D32).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = String.format("%,d", dynamicLocatedCount).replace(',', '.'),
                                color = Color(0xFF4CAF50),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "LOCALIZADOS",
                                color = Color(0xFF4CAF50),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        // Search Bar and Add Report Button
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar persona por nombre o sector...", color = TextPrimary.copy(alpha = 0.5f), fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar", tint = FlagYellow) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear", tint = TextPrimary)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FlagYellow,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = { onShowRegisterDialogChange(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = FlagYellow),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Registrar", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("REGISTRAR NUEVA PERSONA SISMORED", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }

        // Live Search Results
        if (searchQuery.trim().isNotEmpty()) {
            item {
                Text(
                    "RESULTADOS DE BÚSQUEDA (${filteredPrepopulated.size + filteredDbReports.size})",
                    color = FlagYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            
            // Render database custom matches
            items(filteredDbReports) { report ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(GlassBackground)
                        .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("REPORTE REGISTRADO", color = FlagYellow, fontWeight = FontWeight.Black, fontSize = 11.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (report.isSynced) FlagBlue.copy(alpha=0.3f) else ErrorRed.copy(alpha=0.3f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(if (report.isSynced) "SINC" else "OFFLINE", color = if (report.isSynced) FlagBlue else ErrorRed, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(report.description, color = TextPrimary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Coordenadas: Lat ${report.latitude} | Lon ${report.longitude}", color = TextPrimary.copy(alpha=0.6f), fontSize = 10.sp)
                    }
                }
            }

            // Render static matches
            items(filteredPrepopulated) { person ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(GlassBackground)
                        .border(
                            1.dp,
                            when (person.status) {
                                "BUSCADO" -> FlagRed.copy(alpha = 0.5f)
                                "LOCALIZADO" -> Color.Green.copy(alpha = 0.5f)
                                else -> FlagYellow.copy(alpha = 0.5f)
                            },
                            RoundedCornerShape(10.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(person.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when (person.status) {
                                            "BUSCADO" -> FlagRed.copy(alpha = 0.2f)
                                            "LOCALIZADO" -> Color.Green.copy(alpha = 0.2f)
                                            else -> FlagYellow.copy(alpha = 0.2f)
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(person.status, color = if (person.status == "LOCALIZADO") Color.Green else if (person.status == "BUSCADO") FlagRed else FlagYellow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Último Sector: ${person.sector}", color = TextPrimary.copy(alpha=0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(person.description, color = TextPrimary.copy(alpha=0.7f), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(person.timeAgo, color = TextPrimary.copy(alpha=0.5f), fontSize = 9.sp)
                    }
                }
            }
        } else {
            // General people view header
            item {
                Text("CENSO RECIENTE Y REPORTES", color = FlagYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            
            // Show recent entries
            items(prepopulatedPeople.take(3)) { person ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(GlassBackground)
                        .border(1.dp, GlassBorder.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(person.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when (person.status) {
                                            "BUSCADO" -> FlagRed.copy(alpha = 0.2f)
                                            "LOCALIZADO" -> Color.Green.copy(alpha = 0.2f)
                                            else -> FlagYellow.copy(alpha = 0.2f)
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(person.status, color = if (person.status == "LOCALIZADO") Color.Green else if (person.status == "BUSCADO") FlagRed else FlagYellow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Sector: ${person.sector}", color = TextPrimary.copy(alpha=0.8f), fontSize = 11.sp)
                    }
                }
            }
        }

        // MINI MANUAL SECTION
        item {
            Text("MINI MANUAL DE OPERACIÓN DE EMERGENCIA", color = FlagYellow, fontWeight = FontWeight.Black, fontSize = 14.sp)
        }

        // Accordion 1
        item {
            ManualAccordionItem(
                title = "1. REGISTRO Y BÚSQUEDA (Aquí Estoy)",
                expanded = manual0,
                onToggle = { manual0 = !manual0 },
                content = "Esta plataforma centraliza los datos de personas reportadas tras eventos sísmicos. Utiliza la barra de búsqueda para verificar si un familiar ya fue localizado o reportado antes de iniciar un nuevo registro. Esto evita duplicaciones de alertas y optimiza enormemente el trabajo en campo de Protección Civil y la Cruz Roja de Venezuela."
            )
        }

        // Accordion 2
        item {
            ManualAccordionItem(
                title = "2. BIOSENSOR DE ONDAS ACÚSTICAS",
                expanded = manual1,
                onToggle = { manual1 = !manual1 },
                content = "Permite capturar vibraciones mecánicas de baja frecuencia y ruidos bajo escombros. Conecta auriculares para escuchar el canal amplificado y ajusta la sensibilidad de decibelios. Si la amplitud del sonido capturado por el micrófono supera el umbral crítico de manera rítmica, el sistema activa la alerta roja visual '¡ALERTA! SEÑAL DE VIDA DETECTADA' ideal para rescatistas."
            )
        }

        // Accordion 3
        item {
            ManualAccordionItem(
                title = "3. ESCÁNER WI-FI Y BALIZA SOS",
                expanded = manual2,
                onToggle = { manual2 = !manual2 },
                content = "Analiza pings de Wi-Fi y Bluetooth emitidos de forma automática por teléfonos móviles de sobrevivientes atrapados, incluso sin señal celular o chip SIM activo. El radar calcula la distancia aproximada basándose en la atenuación de la señal (RSSI). El sistema muestra la baliza activa con detalles del sector para que localices al objetivo."
            )
        }

        // Accordion 4
        item {
            ManualAccordionItem(
                title = "4. GEOLOCALIZACIÓN GPS FUERA DE LÍNEA",
                expanded = manual3,
                onToggle = { manual3 = !manual3 },
                content = "La pestaña 'Mapa GPS' utiliza el receptor GPS físico integrado en su dispositivo Android. Este chip funciona de forma totalmente gratuita y directa con satélites espaciales, sin necesidad de conexión a internet o saldo telefónico. El sistema permite ver su latitud/longitud exactas, calibrar el rumbo mediante la brújula electrónica integrada y trazar balizas locales."
            )
        }

        // Accordion 5
        item {
            ManualAccordionItem(
                title = "5. COLA DE SINCRONIZACIÓN MESH",
                expanded = manual4,
                onToggle = { manual4 = !manual4 },
                content = "Todos los reportes y registros de personas generados en estado off-grid se guardan de forma encriptada en el almacenamiento local seguro de SQLite (Room). Tan pronto como su dispositivo establezca contacto con una red satelital de emergencia, un nodo de malla WiFi (Mesh), o recupere señal móvil, la cola enviará de manera silenciosa las actualizaciones al servidor central."
            )
        }
    }

    // New report creation dialog
    if (showRegisterDialog) {
        AlertDialog(
            onDismissRequest = { onShowRegisterDialogChange(false) },
            title = { Text("Registrar Reporte de Persona", color = FlagYellow, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = registerName,
                        onValueChange = onRegisterNameChange,
                        label = { Text("Nombre Completo") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FlagYellow, cursorColor = FlagYellow),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = registerLocationText,
                        onValueChange = onRegisterLocationTextChange,
                        label = { Text("Sector Visto (Ej: Macuto, Vargas)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FlagYellow, cursorColor = FlagYellow),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = registerDescription,
                        onValueChange = onRegisterDescriptionChange,
                        label = { Text("Datos adicionales o vestimenta") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FlagYellow, cursorColor = FlagYellow),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text("Estado:", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Buscado", "Localizado", "Reportado").forEach { status ->
                            val isSelected = registerStatus == status
                            FilterChip(
                                selected = isSelected,
                                onClick = { onRegisterStatusChange(status) },
                                label = { Text(status, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when(status) {
                                        "Buscado" -> FlagRed.copy(alpha=0.25f)
                                        "Localizado" -> Color.Green.copy(alpha=0.25f)
                                        else -> FlagYellow.copy(alpha=0.25f)
                                    },
                                    selectedLabelColor = when(status) {
                                        "Buscado" -> FlagRed
                                        "Localizado" -> Color(0xFF4CAF50)
                                        else -> FlagYellow
                                    }
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (registerName.trim().isEmpty()) {
                            Toast.makeText(context, "Por favor ingrese el nombre", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        onSaveReport()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlagYellow, contentColor = Color.Black)
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { onShowRegisterDialogChange(false) }) {
                    Text("Cancelar", color = TextPrimary.copy(alpha=0.6f))
                }
            },
            containerColor = Color(0xFF1A1A26)
        )
    }
}

@Composable
fun ManualAccordionItem(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = GlassBackground),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .border(1.dp, if (expanded) FlagYellow.copy(alpha=0.6f) else GlassBorder.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    title,
                    color = if (expanded) FlagYellow else TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = "Expandir",
                    tint = if (expanded) FlagYellow else TextPrimary
                )
            }
            
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        content,
                        color = TextPrimary.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

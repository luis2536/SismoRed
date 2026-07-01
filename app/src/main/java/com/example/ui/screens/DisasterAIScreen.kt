package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.theme.*
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisasterAIScreen(onNavigateBack: () -> Unit) {
    var prompt by remember { mutableStateOf("") }
    var chatHistory by remember { mutableStateOf(listOf(
        Pair("system", "Asistente IA de SismoRedVen activado. Analizando protocolos de desastre. ¿Cuál es su emergencia?")
    )) }
    var isTyping by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val generativeModel = remember {
        GenerativeModel(
            modelName = "gemini-1.5-pro",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asistente IA SismoRed", color = FlagYellow, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = FlagYellow)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MatrixDark)
            )
        },
        containerColor = MatrixDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(chatHistory) { message ->
                    val isUser = message.first == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 0.dp,
                                    bottomEnd = if (isUser) 0.dp else 16.dp
                                ))
                                .background(if (isUser) FlagBlue.copy(alpha = 0.2f) else GlassBackground)
                                .border(1.dp, if (isUser) FlagBlue else GlassBorder, RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 0.dp,
                                    bottomEnd = if (isUser) 0.dp else 16.dp
                                ))
                                .padding(12.dp)
                                .fillMaxWidth(0.85f)
                        ) {
                            Column {
                                Text(
                                    if (isUser) "Rescatista" else "IA Gemini",
                                    color = if (isUser) FlagYellow else FlagRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(message.second, color = TextPrimary, fontSize = 14.sp)
                            }
                        }
                    }
                }
                if (isTyping) {
                    item {
                        Text("IA procesando telemetría...", color = FlagYellow, fontSize = 12.sp, modifier = Modifier.padding(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    placeholder = { Text("Describa la situación...", color = TextPrimary.copy(alpha=0.5f)) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FlagYellow,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (prompt.isNotBlank() && !isTyping) {
                            val userMsg = prompt
                            chatHistory = chatHistory + Pair("user", userMsg)
                            prompt = ""
                            isTyping = true
                            coroutineScope.launch {
                                try {
                                    val response = generativeModel.generateContent(
                                        "Eres un asistente de rescate avanzado para emergencias sísmicas en Venezuela llamado SismoRedVen. Responde de forma técnica pero breve y útil a este reporte de un rescatista: $userMsg"
                                    )
                                    chatHistory = chatHistory + Pair("ai", response.text ?: "Sin respuesta.")
                                } catch (e: Exception) {
                                    chatHistory = chatHistory + Pair("ai", "Error de conexión con satélite de IA. Intente de nuevo.")
                                } finally {
                                    isTyping = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.background(FlagYellow, RoundedCornerShape(24.dp))
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Enviar", tint = MatrixDark)
                }
            }
        }
    }
}

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ChatMessageEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    
    // Fake messages for simulation
    val messages = remember { mutableStateListOf(
        ChatMessageEntity(1, "system", "Syntropy Node", "Conexión encriptada estable. Malla VPN activa.", System.currentTimeMillis() - 60000, true),
        ChatMessageEntity(2, "user_1", "Rescatista Alpha", "¿Alguien en el sector 4? Necesitamos soporte médico.", System.currentTimeMillis() - 30000, true)
    ) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Global P2P Chat", color = NeonCyan, fontWeight = FontWeight.Bold) },
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
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                reverseLayout = false
            ) {
                items(messages) { msg ->
                    ChatBubble(msg)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassBackground)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Transmitir en malla...", color = TextPrimary.copy(alpha=0.5f)) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            messages.add(
                                ChatMessageEntity(
                                    id = messages.size + 1,
                                    senderId = "me",
                                    senderName = "Yo",
                                    message = messageText,
                                    timestamp = System.currentTimeMillis(),
                                    isSynced = false // Will be synced by background worker
                                )
                            )
                            messageText = ""
                        }
                    },
                    modifier = Modifier
                        .background(NeonCyan, RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Enviar", tint = MatrixDark)
                }
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessageEntity) {
    val isMe = msg.senderId == "me"
    val isSystem = msg.senderId == "system"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .clip(RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 0.dp,
                    bottomEnd = if (isMe) 0.dp else 16.dp
                ))
                .background(if (isSystem) GlassBorder else if (isMe) NeonCyan.copy(alpha = 0.2f) else GlassBackground)
                .border(1.dp, if (isSystem) NeonCyan else if (isMe) NeonCyan else GlassBorder, RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 0.dp,
                    bottomEnd = if (isMe) 0.dp else 16.dp
                ))
                .padding(12.dp)
        ) {
            Column {
                if (!isMe) {
                    Text(msg.senderName, color = if (isSystem) NeonCyan else NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(msg.message, color = TextPrimary, fontSize = 14.sp)
            }
        }
    }
}

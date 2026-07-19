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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ChatMessageEntity
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var messageText by remember { mutableStateOf("") }

    // Initialize Room DB
    val db = remember { Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "resq-net-db").build() }
    val chatDao = remember { db.chatDao() }
    
    // Collect persisted messages
    val messagesState by chatDao.getAllMessages().collectAsState(initial = emptyList())

    // If database is empty, prepopulate with critical tactical systems logs
    LaunchedEffect(messagesState) {
        if (messagesState.isEmpty()) {
            coroutineScope.launch {
                chatDao.insertMessage(
                    ChatMessageEntity(
                        senderId = "system",
                        senderName = "Núcleo Central",
                        message = "Conexión encriptada estable. Malla VPN activa.",
                        timestamp = System.currentTimeMillis() - 60000,
                        isSynced = true
                    )
                )
                chatDao.insertMessage(
                    ChatMessageEntity(
                        senderId = "user_1",
                        senderName = "Rescatista Alpha",
                        message = "¿Alguien en el sector 4? Necesitamos soporte sismológico.",
                        timestamp = System.currentTimeMillis() - 30000,
                        isSynced = true
                    )
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat Global P2P", color = FlagYellow, fontWeight = FontWeight.Bold) },
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
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                reverseLayout = false
            ) {
                items(messagesState) { msg ->
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
                        focusedBorderColor = FlagYellow,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            val userMsg = messageText
                            messageText = ""
                            coroutineScope.launch {
                                chatDao.insertMessage(
                                    ChatMessageEntity(
                                        senderId = "me",
                                        senderName = "Yo",
                                        message = userMsg,
                                        timestamp = System.currentTimeMillis(),
                                        isSynced = false
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .background(FlagYellow, RoundedCornerShape(8.dp))
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
                .background(if (isSystem) GlassBorder else if (isMe) FlagYellow.copy(alpha = 0.2f) else GlassBackground)
                .border(1.dp, if (isSystem) FlagYellow else if (isMe) FlagYellow else GlassBorder, RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 0.dp,
                    bottomEnd = if (isMe) 0.dp else 16.dp
                ))
                .padding(12.dp)
        ) {
            Column {
                if (!isMe) {
                    Text(msg.senderName, color = if (isSystem) FlagYellow else FlagBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(msg.message, color = TextPrimary, fontSize = 14.sp)
            }
        }
    }
}

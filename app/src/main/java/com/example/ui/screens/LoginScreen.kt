package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    var isRegistering by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Rescatista") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MatrixDark),
        contentAlignment = Alignment.Center
    ) {
        // Subtle 3D background image
        Image(
            painter = painterResource(id = R.drawable.vzla_flag_3d_bg_1783071278886),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.6f
        )
        
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp), ambientColor = FlagBlue, spotColor = FlagYellow)
                .clip(RoundedCornerShape(24.dp))
                .background(GlassBackground.copy(alpha = 0.85f))
                .border(1.dp, GlassBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("SISMORED VEN", color = FlagYellow, fontWeight = FontWeight.Black, fontSize = 28.sp, letterSpacing = 2.sp)
            Text("NÚCLEO CENTRAL", color = FlagBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            
            Spacer(modifier = Modifier.height(24.dp))

            if (isRegistering) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(GlassBorder),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "Foto de Perfil", tint = TextPrimary, modifier = Modifier.size(48.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre Completo", color = TextPrimary.copy(alpha=0.7f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FlagYellow,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono (WhatsApp)", color = TextPrimary.copy(alpha=0.7f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FlagYellow,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = cedula,
                onValueChange = { cedula = it },
                label = { Text("ID Cédula", color = TextPrimary.copy(alpha=0.7f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FlagYellow,
                    unfocusedBorderColor = GlassBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Clave de Acceso", color = TextPrimary.copy(alpha=0.7f)) },
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FlagYellow,
                    unfocusedBorderColor = GlassBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isRegistering) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    RadioButton(
                        selected = role == "Rescatista",
                        onClick = { role = "Rescatista" },
                        colors = RadioButtonDefaults.colors(selectedColor = FlagBlue, unselectedColor = GlassBorder)
                    )
                    Text("Rescatista", color = TextPrimary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(
                        selected = role == "Civil",
                        onClick = { role = "Civil" },
                        colors = RadioButtonDefaults.colors(selectedColor = FlagYellow, unselectedColor = GlassBorder)
                    )
                    Text("Civil", color = TextPrimary, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { /* Lógica biométrica */ },
                    colors = ButtonDefaults.buttonColors(containerColor = GlassBorder, contentColor = TextPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Fingerprint, contentDescription = "Biometría")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Vincular Datos Biométricos")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = onLoginSuccess,
                colors = ButtonDefaults.buttonColors(containerColor = FlagYellow, contentColor = MatrixDark),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isRegistering) "REGISTRAR Y ACCEDER" else "INICIAR SESIÓN", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { isRegistering = !isRegistering }) {
                Text(if (isRegistering) "¿Ya tienes cuenta? Inicia Sesión" else "¿No tienes cuenta? Regístrate", color = FlagBlue)
            }
        }
    }
}
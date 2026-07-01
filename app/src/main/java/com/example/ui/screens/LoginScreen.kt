package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Rescatista") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MatrixDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(GlassBackground)
                .border(2.dp, GlassBorder, RoundedCornerShape(16.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("SISMORED VEN", color = FlagYellow, fontWeight = FontWeight.Black, fontSize = 28.sp, letterSpacing = 2.sp)
            Text("NÚCLEO CENTRAL", color = FlagBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("ID Cédula / Alias", color = TextPrimary.copy(alpha=0.7f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FlagYellow,
                    unfocusedBorderColor = GlassBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                RadioButton(
                    selected = role == "Rescatista",
                    onClick = { role = "Rescatista" },
                    colors = RadioButtonDefaults.colors(selectedColor = FlagBlue, unselectedColor = GlassBorder)
                )
                Text("Rescatista", color = TextPrimary)
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = role == "Civil",
                    onClick = { role = "Civil" },
                    colors = RadioButtonDefaults.colors(selectedColor = FlagYellow, unselectedColor = GlassBorder)
                )
                Text("Civil", color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLoginSuccess,
                colors = ButtonDefaults.buttonColors(containerColor = FlagYellow, contentColor = MatrixDark),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("INICIAR SESIÓN", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}

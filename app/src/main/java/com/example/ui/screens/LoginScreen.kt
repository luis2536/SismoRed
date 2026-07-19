package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

import androidx.compose.ui.platform.LocalContext
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import android.widget.Toast

import androidx.compose.ui.res.stringResource
import com.example.R

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    var isRegistering by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Rescatista") }
    var isAuthenticating by remember { mutableStateOf(false) }

    fun authenticateWithBiometrics() {
        val fragmentActivity = context as? FragmentActivity ?: return
        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(fragmentActivity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(context, "Error Biométrico: $errString", Toast.LENGTH_SHORT).show()
                }
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(context, "Autenticación Biométrica Exitosa", Toast.LENGTH_SHORT).show()
                    onLoginSuccess()
                }
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(context, "Autenticación Fallida", Toast.LENGTH_SHORT).show()
                }
            })

        val title = context.getString(R.string.auth_biometric_title)
        val subtitle = context.getString(R.string.auth_biometric_subtitle)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Usar Contraseña")
            .build()
        
        biometricPrompt.authenticate(promptInfo)
    }

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
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.login_title), color = FlagYellow, fontWeight = FontWeight.Black, fontSize = 28.sp, letterSpacing = 2.sp)
            Text(stringResource(R.string.login_subtitle), color = FlagBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            
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
                    onClick = { authenticateWithBiometrics() },
                    colors = ButtonDefaults.buttonColors(containerColor = GlassBorder, contentColor = TextPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Fingerprint, contentDescription = "Biometría")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.login_biometric_btn))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { authenticateWithBiometrics() },
                colors = ButtonDefaults.buttonColors(containerColor = FlagYellow, contentColor = MatrixDark),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isRegistering) stringResource(R.string.login_btn_register) else stringResource(R.string.login_btn_signin), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { isRegistering = !isRegistering }) {
                Text(if (isRegistering) "¿Ya tienes cuenta? Inicia Sesión" else "¿No tienes cuenta? Regístrate", color = FlagBlue)
            }
        }
    }
}
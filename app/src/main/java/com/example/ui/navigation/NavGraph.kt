package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.MainMapOfflineScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.FaceScanScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.DisasterAIScreen

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("home") { popUpTo("login") { inclusive = true } } }
            )
        }
        composable("home") {
            MainMapOfflineScreen(
                onNavigateToChat = { navController.navigate("chat") },
                onNavigateToFaceScan = { navController.navigate("face_scan") },
                onNavigateToAI = { navController.navigate("ai_assistant") }
            )
        }
        composable("chat") {
            ChatScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("face_scan") {
            FaceScanScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("ai_assistant") {
            DisasterAIScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

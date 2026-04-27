package com.example.escapelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Games
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.escapelab.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EscapeLabTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val startDestination = if (authViewModel.isLoggedIn()) "main" else "login"

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("login") {
                        LoginScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = {
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onGoToSignUp = { navController.navigate("signup") }
                        )
                    }
                    composable("signup") {
                        SignUpScreen(
                            viewModel = authViewModel,
                            onSignUpSuccess = {
                                navController.navigate("main") {
                                    popUpTo("signup") { inclusive = true }
                                }
                            },
                            onGoToLogin = { navController.popBackStack() }
                        )
                    }
                    composable("main") {
                        MainScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    var activeSessionCode by remember { mutableStateOf("") }
    val gameViewModel: GameViewModel = viewModel()
    val lobbyViewModel: LobbyViewModel = viewModel()

    Box(modifier = Modifier.fillMaxSize()) {
        when (selectedTab) {
            0 -> HomeScreen(onBuildRoom = { selectedTab = 1 })
            1 -> {
                val roomBuilderViewModel: RoomBuilderViewModel = viewModel()
                RoomBuilderScreen(
                    viewModel = roomBuilderViewModel,
                    onRoomSaved = {
                        roomBuilderViewModel.resetState()
                        selectedTab = 2
                    }
                )
            }
            2 -> ProfileScreen()
            3 -> {
                LaunchedEffect(selectedTab) {
                    lobbyViewModel.resetSession()
                }
                LobbyScreen(
                    viewModel = lobbyViewModel,
                    onGameStart = { code ->
                        activeSessionCode = code
                        gameViewModel.init(code)
                        selectedTab = 4
                    }
                )
            }
            4 -> {
                val session by gameViewModel.session.collectAsState()
                when (session?.status) {
                    "finished" -> VictoryScreen(
                        onReturnHome = {
                            activeSessionCode = ""
                            selectedTab = 0
                        }
                    )
                    else -> PuzzleScreen(
                        viewModel = gameViewModel,
                        onGameFinished = {
                            activeSessionCode = ""
                            selectedTab = 0
                        }
                    )
                }
            }
        }

        // Hide bottom nav during active game
        if (selectedTab != 4) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(BackgroundMid)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    NavItem(
                        icon = Icons.Filled.Home,
                        label = "HOME",
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    NavItem(
                        icon = Icons.Filled.Build,
                        label = "BUILD",
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                    NavItem(
                        icon = Icons.Filled.Person,
                        label = "PROFILE",
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 }
                    )
                    NavItem(
                        icon = Icons.Filled.Games,
                        label = "PLAY",
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 }
                    )
                }
            }
        }
    }
}

@Composable
fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) AmberGlow else ParchmentDim,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            fontSize = 9.sp,
            color = if (selected) AmberGlow else ParchmentDim,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
package com.example.escapelab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.escapelab.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
@Composable
fun LobbyScreen(
    viewModel: LobbyViewModel,
    onGameStart: (String) -> Unit
) {
    val sessionCode by viewModel.sessionCode.collectAsState()
    val players by viewModel.players.collectAsState()
    val session by viewModel.session.collectAsState()
    val joinCode by viewModel.joinCode.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var myRooms by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var selectedRoomId by remember { mutableStateOf("") }
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    LaunchedEffect(Unit) {
        db.collection("rooms")
            .whereEqualTo("creatorId", auth.currentUser?.uid ?: "")
            .get()
            .addOnSuccessListener { result ->
                myRooms = result.documents.mapNotNull { doc ->
                    doc.data?.toMutableMap()?.apply { put("roomId", doc.id) }
                }
            }
    }

    LaunchedEffect(session?.status) {
        if (session?.status == "playing") {
            onGameStart(sessionCode)
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text("EscapeLab", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(4.dp))
                Text("// LOBBY", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(24.dp))
            }

            if (sessionCode.isEmpty()) {

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AmberDim, RoundedCornerShape(8.dp))
                            .background(BackgroundCard, RoundedCornerShape(8.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                "// HOST A ROOM",
                                style = MaterialTheme.typography.labelLarge,
                                fontSize = 10.sp,
                                color = AmberDim
                            )
                            Spacer(Modifier.height(12.dp))

                            if (myRooms.isEmpty()) {
                                Text(
                                    "No rooms found. Build a room first!",
                                    color = ParchmentDim,
                                    fontSize = 13.sp
                                )
                            } else {
                                Text(
                                    "Select a room to host:",
                                    color = ParchmentDim,
                                    fontSize = 13.sp
                                )
                                Spacer(Modifier.height(8.dp))

                                myRooms.forEach { room ->
                                    val roomId = room["roomId"] as? String ?: ""
                                    val title = room["title"] as? String ?: "Untitled"
                                    val playerCount = (room["playerCount"] as? Long)?.toInt() ?: 2
                                    val isSelected = selectedRoomId == roomId

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .border(
                                                1.dp,
                                                if (isSelected) AmberGlow else AmberDim.copy(alpha = 0.4f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .background(
                                                if (isSelected) AmberDim.copy(alpha = 0.2f)
                                                else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedRoomId = roomId }
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(title, color = Parchment, fontSize = 14.sp)
                                            Text(
                                                "$playerCount players",
                                                color = ParchmentDim,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    val playerCount = (myRooms.find {
                                        it["roomId"] == selectedRoomId
                                    }?.get("playerCount") as? Long)?.toInt() ?: 2
                                    viewModel.createSession(selectedRoomId, playerCount)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                enabled = selectedRoomId.isNotEmpty() && !isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AmberMid,
                                    contentColor = Color.Black
                                )
                            ) {
                                Text(
                                    if (isLoading) "Creating..." else "Create Session",
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = AmberDim.copy(alpha = 0.3f)
                        )
                        Text("  or  ", color = ParchmentDim, fontSize = 12.sp)
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = AmberDim.copy(alpha = 0.3f)
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AmberDim, RoundedCornerShape(8.dp))
                            .background(BackgroundCard, RoundedCornerShape(8.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                "// JOIN A ROOM",
                                style = MaterialTheme.typography.labelLarge,
                                fontSize = 10.sp,
                                color = AmberDim
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = joinCode,
                                onValueChange = { viewModel.updateJoinCode(it) },
                                label = { Text("Enter 4-letter code", color = Parchment) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Characters
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Parchment,
                                    unfocusedTextColor = Parchment,
                                    focusedBorderColor = AmberMid,
                                    unfocusedBorderColor = AmberDim,
                                    cursorColor = AmberGlow
                                )
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.joinSession() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                enabled = joinCode.length == 4 && !isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AmberMid,
                                    contentColor = Color.Black
                                )
                            ) {
                                Text(
                                    if (isLoading) "Joining..." else "Join Session",
                                    color = Color.Black
                                )
                            }

                            if (errorMessage.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Text(errorMessage, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

            } else {

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AmberGlow, RoundedCornerShape(8.dp))
                            .background(BackgroundCard, RoundedCornerShape(8.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "// ROOM CODE",
                                style = MaterialTheme.typography.labelLarge,
                                fontSize = 10.sp,
                                color = AmberDim
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                sessionCode,
                                style = MaterialTheme.typography.headlineLarge,
                                fontSize = 48.sp,
                                color = AmberGlow,
                                textAlign = TextAlign.Center,
                                letterSpacing = 8.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Share this code with your players",
                                color = ParchmentDim,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                item {
                    Text(
                        "// PLAYERS IN LOBBY — ${players.size}",
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 10.sp,
                        color = AmberDim
                    )
                }

                items(players) { player ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AmberDim.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .background(BackgroundCard, RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Player",
                                tint = ParchmentDim,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(Modifier.width(12.dp))
                            Text(player.displayName, color = Parchment, fontSize = 14.sp)
                        }
                        if (player.userId == session?.hostId) {
                            Text(
                                "HOST",
                                color = AmberGlow,
                                fontSize = 10.sp,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                if (session?.hostId == viewModel.getCurrentUserId()) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.startGame() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = players.size >= 1,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberMid,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                "Start Game →",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Black
                            )
                        }
                        if (players.size < 2) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "You can start solo or wait for more players",
                                color = ParchmentDim,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Waiting for host to start the game...",
                                color = ParchmentDim,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
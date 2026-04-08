package com.example.escapelab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.escapelab.ui.theme.*

@Composable
fun ProfileScreen() {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""
    val userEmail = auth.currentUser?.email ?: "Unknown"

    var createdRooms by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        db.collection("rooms")
            .whereEqualTo("creatorId", userId)
            .get()
            .addOnSuccessListener { result ->
                createdRooms = result.documents.mapNotNull { it.data }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
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
                .padding(horizontal = 20.dp)
                .padding(bottom = 80.dp)
        ) {
            item {
                Spacer(Modifier.height(20.dp))
                Text(
                    "Profile",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    userEmail,
                    color = ParchmentDim,
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    "// MY CREATED ROOMS",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 10.sp,
                    color = AmberDim
                )
                Spacer(Modifier.height(12.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AmberMid)
                    }
                } else if (createdRooms.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AmberDim.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No rooms created yet.\nTap Build to create your first room!",
                            color = ParchmentDim,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            items(createdRooms.size) { index ->
                val room = createdRooms[index]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AmberDim.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .background(BackgroundCard, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            "// MY ROOM",
                            color = AmberDim,
                            fontSize = 9.sp,
                            fontStyle = FontStyle.Italic
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            room["title"] as? String ?: "Untitled Room",
                            style = MaterialTheme.typography.labelLarge,
                            color = Parchment,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${room["playerCount"] ?: 2} players",
                            color = ParchmentDim,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.escapelab.ui.theme.*

data class MockRoom(
    val title: String,
    val description: String,
    val stages: Int,
    val players: Int,
    val tag: String
)

val mockRooms = listOf(
    MockRoom("The Haunted Librarian", "A cursed archivist left ciphers in the stacks. Decode the ancient texts before midnight.", 3, 2, "FEATURED"),
    MockRoom("Laboratory X", "A mad scientist's lab holds dark secrets. Can your team escape before the experiment ends?", 2, 3, "NEW"),
    MockRoom("The Clock Tower", "Time is running out. Literally. Solve the mechanical puzzles before the hour strikes.", 4, 2, "HARD"),
    MockRoom("Haunted Manor", "Every room hides a clue. Every clue hides a secret. Not all secrets want to be found.", 2, 2, "QUICK"),
    MockRoom("The Forgotten Tomb", "An ancient curse binds you to these halls. Only knowledge can set you free.", 5, 3, "FEATURED")
)

@Composable
fun HomeScreen(
    onBuildRoom: () -> Unit
) {
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
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "EscapeLab",
                        style = MaterialTheme.typography.headlineLarge,
                        fontSize = 24.sp
                    )
                    Text("👤", fontSize = 24.sp)
                }

                Spacer(Modifier.height(20.dp))

                // Hero section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AmberDim, RoundedCornerShape(8.dp))
                        .background(BackgroundCard, RoundedCornerShape(8.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            "Enter the Unknown",
                            style = MaterialTheme.typography.headlineLarge,
                            fontSize = 20.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Design, share, and solve handcrafted escape rooms",
                            color = ParchmentDim,
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {},
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AmberMid,
                                    contentColor = Color.Black
                                )
                            ) {
                                Text("Browse", color = Color.Black, fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = onBuildRoom,
                                modifier = Modifier.weight(1f),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(AmberMid)
                                )
                            ) {
                                Text("Build a Room", color = AmberMid, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Section label
                Text(
                    "// FEATURED ROOMS",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 10.sp,
                    color = AmberDim
                )
                Spacer(Modifier.height(12.dp))
            }

            items(mockRooms.size) { index ->
                val room = mockRooms[index]
                RoomCard(room = room)
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun RoomCard(room: MockRoom) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AmberDim.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .background(BackgroundCard, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                "// ${room.tag} · ${room.stages} STAGES · ${room.players} PLAYERS",
                fontStyle = FontStyle.Italic,
                color = AmberDim,
                fontSize = 9.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                room.title,
                style = MaterialTheme.typography.labelLarge,
                color = Parchment,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                room.description,
                color = ParchmentDim,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberDim,
                    contentColor = Parchment
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text("Play", fontSize = 12.sp, color = Parchment)
            }
        }
    }
}
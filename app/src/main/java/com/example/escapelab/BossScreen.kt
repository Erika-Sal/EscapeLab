package com.example.escapelab

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.escapelab.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
@Composable
fun BossScreen(
    viewModel: GameViewModel,
    onGameFinished: () -> Unit
) {
    val players by viewModel.players.collectAsState()
    val session by viewModel.session.collectAsState()

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()

    val userId = auth.currentUser?.uid ?: ""
    val sessionCode = session?.sessionCode ?: ""
    var showChat by remember { mutableStateOf(false) }

    var myClue by remember { mutableStateOf("") }
    var myDigit by remember { mutableStateOf(-1) }
    var codeInput by remember { mutableStateOf("") }
    var submitResult by remember { mutableStateOf<Boolean?>(null) }
    var hasSubmitted by remember { mutableStateOf(false) }
    var correctCode by remember { mutableStateOf("") }



    LaunchedEffect(userId) {
        try {
            val playerDoc = db.collection("sessions")
                .document(sessionCode)
                .collection("players")
                .document(userId)
                .get().await()

            val existingDigit = playerDoc.getLong("bossDigit")?.toInt()
            val existingClue = playerDoc.getString("bossClue")

            if (existingDigit != null && existingClue != null) {
                myDigit = existingDigit
                myClue = existingClue
            } else {
                val allPlayers = db.collection("sessions")
                    .document(sessionCode)
                    .collection("players")
                    .get().await()

                val playerIds = allPlayers.documents.map { it.id }.sorted()
                val myIndex = playerIds.indexOf(userId)

                val sessionDoc = db.collection("sessions")
                    .document(sessionCode).get().await()

                var code = sessionDoc.getString("bossCode")
                if (code == null) {
                    val playerCount = session?.playerCount ?: 2
                    val digits = generateBossCode(playerCount)
                    code = digits.joinToString("")
                    db.collection("sessions").document(sessionCode)
                        .update("bossCode", code).await()
                }

                val digit = code[myIndex.coerceIn(0, code.length - 1)].toString().toInt()
                val clue = getClueForDigit(digit)

                db.collection("sessions")
                    .document(sessionCode)
                    .collection("players")
                    .document(userId)
                    .update(
                        mapOf(
                            "bossDigit" to digit,
                            "bossClue" to clue,
                            "bossSubmitted" to false
                        )
                    ).await()

                myDigit = digit
                myClue = clue
            }

            val sessionDoc = db.collection("sessions")
                .document(sessionCode).get().await()
            correctCode = sessionDoc.getString("bossCode") ?: ""

        } catch (e: Exception) { }
    }

    LaunchedEffect(players) {
        val allDone = players.isNotEmpty() && players.all {
            it.bossButtonTaps == 1
        }
        if (allDone && players.isNotEmpty()) {
            try {
                db.collection("sessions")
                    .document(sessionCode)
                    .update("status", "finished").await()
            } catch (e: Exception) { }
        }
    }

    LaunchedEffect(session?.status) {
        if (session?.status == "finished") onGameFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "✦ FINAL STAGE ✦",
                    style = MaterialTheme.typography.labelLarge,
                    color = AmberDim,
                    fontSize = 11.sp,
                    letterSpacing = 4.sp
                )
                Button(
                    onClick = { showChat = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BackgroundCard,
                        contentColor = Parchment
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Chat,
                            contentDescription = "Chat",
                            tint = Parchment,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("Chat", fontSize = 13.sp, color = Parchment)
                    }

                }
            }

            Spacer(Modifier.height(8.dp))



            Spacer(Modifier.height(8.dp))
            Text(
                "The Vault Seal",
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 28.sp,
                color = Color(0xFFE05555)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Each of you holds a fragment of the ancient key.\nDecode your clue. Share your digit. Enter the code.",
                color = ParchmentDim,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(24.dp))

            if (myClue.isEmpty()) {
                CircularProgressIndicator(color = AmberMid)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AmberGlow, RoundedCornerShape(8.dp))
                        .background(BackgroundCard, RoundedCornerShape(8.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            "// YOUR CLUE",
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = 10.sp,
                            color = AmberDim
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "\"$myClue\"",
                            color = Parchment,
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Your digit is position ${
                                players.indexOfFirst { it.userId == userId }
                                    .let { if (it == -1) "?" else (it + 1).toString() }
                            } in the code",
                            color = ParchmentDim,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                if (!hasSubmitted) {
                    Text(
                        "// ENTER THE FULL ${session?.playerCount ?: 2}-DIGIT CODE",
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 10.sp,
                        color = AmberDim
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = {
                            if (it.length <= (session?.playerCount ?: 2)) {
                                codeInput = it.filter { c -> c.isDigit() }
                            }
                        },
                        label = {
                            Text(
                                "Enter ${session?.playerCount ?: 2}-digit code",
                                color = Parchment
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Parchment,
                            unfocusedTextColor = Parchment,
                            focusedBorderColor = AmberMid,
                            unfocusedBorderColor = AmberDim,
                            cursorColor = AmberGlow
                        )
                    )
                    Spacer(Modifier.height(8.dp))

                    AnimatedVisibility(visible = submitResult == false) {
                        Text(
                            "✗ Wrong code — coordinate with your team!",
                            color = Color(0xFFE05555),
                            fontSize = 13.sp
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    // Always re-fetch the code fresh right before checking
                                    val freshSession = db.collection("sessions")
                                        .document(sessionCode).get().await()
                                    val freshCode = freshSession.getString("bossCode") ?: ""

                                    val isCorrect = codeInput == freshCode
                                    submitResult = isCorrect
                                    if (isCorrect) {
                                        hasSubmitted = true
                                        db.collection("sessions")
                                            .document(sessionCode)
                                            .collection("players")
                                            .document(userId)
                                            .update("bossButtonTaps", 1).await()
                                    }
                                } catch (e: Exception) { }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = codeInput.length == (session?.playerCount ?: 2),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE05555),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            "Submit Code",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                Color(0xFF55E09A).copy(alpha = 0.5f),
                                RoundedCornerShape(8.dp)
                            )
                            .background(
                                Color(0xFF55E09A).copy(alpha = 0.05f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "✓ Code Accepted!",
                                color = Color(0xFF55E09A),
                                fontSize = 20.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Waiting for teammates...",
                                color = ParchmentDim,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "// TEAM STATUS",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 10.sp,
                    color = AmberDim
                )
                Spacer(Modifier.height(8.dp))
                players.forEach { player ->
                    val done = player.bossButtonTaps == 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (done) Icons.Filled.Check else Icons.Filled.HourglassEmpty,
                                contentDescription = null,
                                tint = if (done) Color(0xFF55E09A) else ParchmentDim,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(Modifier.width(8.dp))
                            Text(player.displayName, color = Parchment, fontSize = 13.sp)
                        }
                        Text(
                            if (done) "Code entered!" else "Decoding...",
                            color = if (done) Color(0xFF55E09A) else ParchmentDim,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        ChatOverlay(
            viewModel = viewModel,
            isVisible = showChat,
            onDismiss = { showChat = false }
        )
    }
}
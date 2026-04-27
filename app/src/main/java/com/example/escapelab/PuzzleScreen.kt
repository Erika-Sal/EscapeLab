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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.escapelab.ui.theme.*

@Composable
fun PuzzleScreen(
    viewModel: GameViewModel,
    onGameFinished: () -> Unit
) {
    val session by viewModel.session.collectAsState()
    val players by viewModel.players.collectAsState()
    val myPuzzle by viewModel.myPuzzle.collectAsState()
    val answerInput by viewModel.answerInput.collectAsState()
    val answerResult by viewModel.answerResult.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val timeRemaining by viewModel.timeRemainingSeconds.collectAsState()
    val timerExpired by viewModel.timerExpired.collectAsState()
    var showChat by remember { mutableStateOf(false) }

    LaunchedEffect(session?.status) {
        if (session?.status == "finished") onGameFinished()
    }

    when {
        timerExpired -> TimeUpScreen(onReturnHome = onGameFinished)
        session?.status == "boss" -> BossScreen(
            viewModel = viewModel,
            onGameFinished = onGameFinished
        )
        else -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Spacer(Modifier.height(8.dp))

                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Stage ${(session?.currentStage ?: 0) + 1}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontSize = 20.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👥 ${players.size}", color = ParchmentDim, fontSize = 14.sp)
                            Spacer(Modifier.width(12.dp))
                            Box {
                                Button(
                                    onClick = { showChat = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BackgroundCard,
                                        contentColor = Parchment
                                    ),
                                    contentPadding = PaddingValues(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    )
                                ) {
                                    Text("💬", fontSize = 16.sp)
                                }
                                if (chatMessages.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(
                                                Color(0xFFE05555),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .align(Alignment.TopEnd),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "${chatMessages.size}",
                                            color = Color.White,
                                            fontSize = 8.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Timer bar
                    TimerBar(timeRemainingSeconds = timeRemaining)

                    Spacer(Modifier.height(8.dp))
                    Text(
                        "// YOUR PUZZLE",
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 10.sp,
                        color = AmberDim
                    )
                    Spacer(Modifier.height(16.dp))

                    if (myPuzzle == null) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AmberMid)
                        }
                    } else {
                        // Puzzle card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, AmberDim, RoundedCornerShape(8.dp))
                                .background(BackgroundCard, RoundedCornerShape(8.dp))
                                .padding(20.dp)
                        ) {
                            Column {
                                Text(
                                    myPuzzle!!.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = AmberGlow,
                                    fontSize = 18.sp
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    myPuzzle!!.clueText,
                                    color = Parchment,
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Answer section
                        if (answerResult != AnswerResult.Correct) {
                            OutlinedTextField(
                                value = answerInput,
                                onValueChange = { viewModel.updateAnswer(it) },
                                label = { Text("Your answer...", color = Parchment) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Parchment,
                                    unfocusedTextColor = Parchment,
                                    focusedBorderColor = AmberMid,
                                    unfocusedBorderColor = AmberDim,
                                    cursorColor = AmberGlow
                                )
                            )
                            Spacer(Modifier.height(8.dp))

                            AnimatedVisibility(visible = answerResult == AnswerResult.Wrong) {
                                Text(
                                    "✗ Incorrect — try again",
                                    color = Color(0xFFE05555),
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.submitAnswer() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                enabled = answerInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AmberMid,
                                    contentColor = Color.Black
                                )
                            ) {
                                Text(
                                    "Submit Answer",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.Black
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
                                        "✓ Solved!",
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

                        // Teammate progress
                        Text(
                            "// TEAMMATE PROGRESS",
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = 10.sp,
                            color = AmberDim
                        )
                        Spacer(Modifier.height(8.dp))
                        players.forEach { player ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        if (player.hasSubmittedCorrect) "✓" else "⏳",
                                        fontSize = 14.sp,
                                        color = if (player.hasSubmittedCorrect)
                                            Color(0xFF55E09A) else ParchmentDim
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        player.displayName,
                                        color = Parchment,
                                        fontSize = 13.sp
                                    )
                                }
                                if (player.userId == viewModel.getCurrentUserId()) {
                                    Text("(you)", color = ParchmentDim, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Chat overlay
                ChatOverlay(
                    viewModel = viewModel,
                    isVisible = showChat,
                    onDismiss = { showChat = false }
                )
            }
        }
    }
}
package com.example.escapelab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.escapelab.ui.theme.*

@Composable
fun RoomBuilderScreen(
    viewModel: RoomBuilderViewModel,
    onRoomSaved: () -> Unit
) {
    val roomTitle by viewModel.roomTitle.collectAsState()
    val stages by viewModel.stages.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    LaunchedEffect(saveState) {
        if (saveState is SaveState.Saved) {
            onRoomSaved()
            viewModel.resetState() // reset so builder is fresh next time
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
                Spacer(Modifier.height(16.dp))
                Text(
                    "Room Builder",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "craft your escape",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = roomTitle,
                    onValueChange = { viewModel.updateTitle(it) },
                    label = { Text("Room Title", color = Parchment) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Parchment,
                        unfocusedTextColor = Parchment,
                        focusedBorderColor = AmberMid,
                        unfocusedBorderColor = AmberDim,
                        cursorColor = AmberGlow
                    )
                )
            }

            itemsIndexed(stages) { stageIndex, stage ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AmberDim, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        "Stage ${stageIndex + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        color = AmberGlow
                    )
                    Spacer(Modifier.height(12.dp))

                    stage.puzzles.forEachIndexed { puzzleIndex, puzzle ->
                        Text(
                            "Player ${puzzleIndex + 1} Puzzle",
                            color = ParchmentDim,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = puzzle.title,
                            onValueChange = {
                                viewModel.updatePuzzle(stageIndex, puzzleIndex, puzzle.copy(title = it))
                            },
                            label = { Text("Puzzle Title", color = ParchmentDim) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Parchment,
                                unfocusedTextColor = Parchment,
                                focusedBorderColor = AmberMid,
                                unfocusedBorderColor = AmberDim,
                                cursorColor = AmberGlow
                            )
                        )
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = puzzle.clueText,
                            onValueChange = {
                                viewModel.updatePuzzle(stageIndex, puzzleIndex, puzzle.copy(clueText = it))
                            },
                            label = { Text("Clue", color = ParchmentDim) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Parchment,
                                unfocusedTextColor = Parchment,
                                focusedBorderColor = AmberMid,
                                unfocusedBorderColor = AmberDim,
                                cursorColor = AmberGlow
                            )
                        )
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = puzzle.answer,
                            onValueChange = {
                                viewModel.updatePuzzle(stageIndex, puzzleIndex, puzzle.copy(answer = it))
                            },
                            label = { Text("Answer", color = ParchmentDim) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Parchment,
                                unfocusedTextColor = Parchment,
                                focusedBorderColor = AmberMid,
                                unfocusedBorderColor = AmberDim,
                                cursorColor = AmberGlow
                            )
                        )

                        if (puzzleIndex < stage.puzzles.size - 1) {
                            Spacer(Modifier.height(16.dp))
                            Divider(color = AmberDim.copy(alpha = 0.3f))
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { viewModel.addStage() },
                    modifier = Modifier.fillMaxWidth(),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(AmberDim)
                    )
                ) {
                    Text("+ Add Stage", color = AmberMid)
                }

                Spacer(Modifier.height(16.dp))

                if (saveState is SaveState.Error) {
                    Text(
                        (saveState as SaveState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(8.dp))
                }

                Button(
                    onClick = { viewModel.saveRoom() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = saveState !is SaveState.Saving && roomTitle.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberMid,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        when (saveState) {
                            is SaveState.Saving -> "Saving..."
                            else -> "Save Room"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Black
                    )
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
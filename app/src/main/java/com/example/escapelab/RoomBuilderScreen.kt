package com.example.escapelab

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.escapelab.ui.theme.*

@Composable
fun RoomBuilderScreen(
    viewModel: RoomBuilderViewModel,
    onRoomSaved: () -> Unit
) {
    val roomTitle by viewModel.roomTitle.collectAsState()
    val playerCount by viewModel.playerCount.collectAsState()
    val stages by viewModel.stages.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    LaunchedEffect(saveState) {
        if (saveState is SaveState.Saved) onRoomSaved()
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
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Text("Room Builder", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(4.dp))
                Text("craft your escape", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = roomTitle,
                    onValueChange = { viewModel.updateTitle(it) },
                    label = { Text("Room Title", color = Parchment) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Parchment,
                        unfocusedTextColor = Parchment,
                        focusedBorderColor = AmberMid,
                        unfocusedBorderColor = AmberDim,
                        cursorColor = AmberGlow
                    )
                )
                Spacer(Modifier.height(16.dp))

                Text(
                    "// NUMBER OF PLAYERS",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 10.sp,
                    color = AmberDim
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(2, 3, 4).forEach { count ->
                        val selected = playerCount == count
                        Box(
                            modifier = Modifier
                                .border(
                                    1.dp,
                                    if (selected) AmberGlow else AmberDim,
                                    RoundedCornerShape(8.dp)
                                )
                                .background(
                                    if (selected) AmberDim else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickableNoRipple { viewModel.updatePlayerCount(count) }
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$count players",
                                color = if (selected) AmberGlow else ParchmentDim,
                                fontSize = 13.sp
                            )
                        }
                    }
                }


                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Make room public", color = Parchment, fontSize = 14.sp)
                        Text(
                            "Anyone can find and play this room",
                            color = ParchmentDim,
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Divider(color = AmberDim.copy(alpha = 0.3f))
                Spacer(Modifier.height(16.dp))

                Text(
                    "// STAGES",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 10.sp,
                    color = AmberDim
                )
            }

            itemsIndexed(stages) { stageIndex, stage ->
                StageCard(
                    stageIndex = stageIndex,
                    stage = stage,
                    onUpdatePuzzle = { puzzleIndex, puzzle ->
                        viewModel.updatePuzzle(stageIndex, puzzleIndex, puzzle)
                    },
                    onUpdateImage = { puzzleIndex, uri ->
                        viewModel.updatePuzzleImage(stageIndex, puzzleIndex, uri)
                    },
                    onRemoveStage = { viewModel.removeStage(stageIndex) }
                )
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = saveState !is SaveState.Saving &&
                            roomTitle.isNotBlank() &&
                            stages.isNotEmpty(),
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

@Composable
fun StageCard(
    stageIndex: Int,
    stage: Stage,
    onUpdatePuzzle: (Int, Puzzle) -> Unit,
    onUpdateImage: (Int, String) -> Unit,
    onRemoveStage: () -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AmberDim, RoundedCornerShape(8.dp))
            .background(BackgroundCard, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Stage ${stageIndex + 1}",
                style = MaterialTheme.typography.labelLarge,
                color = AmberGlow,
                fontSize = 16.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text(
                        if (expanded) "▲ Collapse" else "▼ Expand",
                        color = ParchmentDim,
                        fontSize = 11.sp
                    )
                }
                TextButton(
                    onClick = onRemoveStage,
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text("✕ Remove", color = ErrorRed, fontSize = 11.sp)
                }
            }
        }

        if (expanded) {
            Spacer(Modifier.height(12.dp))
            stage.puzzles.forEachIndexed { puzzleIndex, puzzle ->
                PuzzleEditor(
                    puzzleIndex = puzzleIndex,
                    puzzle = puzzle,
                    onUpdate = { onUpdatePuzzle(puzzleIndex, it) },
                    onImageSelected = { onUpdateImage(puzzleIndex, it) }
                )
                if (puzzleIndex < stage.puzzles.size - 1) {
                    Spacer(Modifier.height(12.dp))
                    Divider(color = AmberDim.copy(alpha = 0.2f))
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun PuzzleEditor(
    puzzleIndex: Int,
    puzzle: Puzzle,
    onUpdate: (Puzzle) -> Unit,
    onImageSelected: (String) -> Unit
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.toString()?.let { onImageSelected(it) }
    }

    val playerColors = listOf(AmberGlow, Color(0xFF55A0E0), Color(0xFF55E09A), Color(0xFFE05555))
    val playerColor = playerColors.getOrElse(puzzleIndex) { ParchmentDim }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(playerColor, RoundedCornerShape(50))
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Player ${puzzleIndex + 1} Puzzle",
                color = playerColor,
                fontSize = 13.sp,
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = puzzle.title,
            onValueChange = { onUpdate(puzzle.copy(title = it)) },
            label = { Text("Puzzle Title", color = ParchmentDim) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = fieldColors()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = puzzle.clueText,
            onValueChange = { onUpdate(puzzle.copy(clueText = it)) },
            label = { Text("Clue / Puzzle Description", color = ParchmentDim) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            colors = fieldColors()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = puzzle.answer,
            onValueChange = { onUpdate(puzzle.copy(answer = it)) },
            label = { Text("Answer", color = ParchmentDim) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = fieldColors()
        )
        Spacer(Modifier.height(8.dp))

        if (puzzle.imageUri.isNotEmpty()) {
            AsyncImage(
                model = puzzle.imageUri,
                contentDescription = "Puzzle image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(6.dp))
        }

        OutlinedButton(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(AmberDim)
            )
        ) {
            Text(
                if (puzzle.imageUri.isEmpty()) "📷 Add Image (optional)" else "📷 Change Image",
                color = ParchmentDim,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Parchment,
    unfocusedTextColor = Parchment,
    focusedBorderColor = AmberMid,
    unfocusedBorderColor = AmberDim,
    cursorColor = AmberGlow
)

fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.then(
        Modifier.clickable(
            indication = null,
            interactionSource = androidx.compose.foundation.interaction.MutableInteractionSource()
        ) { onClick() }
    )
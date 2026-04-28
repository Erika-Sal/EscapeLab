package com.example.escapelab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
class RoomBuilderViewModel : ViewModel() {
    private val repository = RoomRepository()

    val roomTitle = MutableStateFlow("")
    val playerCount = MutableStateFlow(2)
    val stages = MutableStateFlow<List<Stage>>(emptyList())

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState
    private var roomId: String = ""
    fun updateTitle(title: String) { roomTitle.value = title }


    fun updatePlayerCount(count: Int) {
        playerCount.value = count
        stages.value = stages.value.map { stage ->
            val currentPuzzles = stage.puzzles.toMutableList()
            while (currentPuzzles.size < count) {
                currentPuzzles.add(Puzzle(playerIndex = currentPuzzles.size))
            }
            while (currentPuzzles.size > count) {
                currentPuzzles.removeAt(currentPuzzles.size - 1)
            }
            stage.copy(puzzles = currentPuzzles)
        }
    }

    fun addStage() {
        val current = stages.value.toMutableList()
        val puzzles = (0 until playerCount.value).map { Puzzle(playerIndex = it) }
        current.add(Stage(stageNumber = current.size, puzzles = puzzles))
        stages.value = current
    }

    fun updatePuzzle(stageIndex: Int, puzzleIndex: Int, puzzle: Puzzle) {
        val currentStages = stages.value.toMutableList()
        val currentPuzzles = currentStages[stageIndex].puzzles.toMutableList()
        currentPuzzles[puzzleIndex] = puzzle
        currentStages[stageIndex] = currentStages[stageIndex].copy(puzzles = currentPuzzles)
        stages.value = currentStages
    }

    fun updatePuzzleImage(stageIndex: Int, puzzleIndex: Int, imageUri: String) {
        val currentStages = stages.value.toMutableList()
        val currentPuzzles = currentStages[stageIndex].puzzles.toMutableList()
        currentPuzzles[puzzleIndex] = currentPuzzles[puzzleIndex].copy(imageUri = imageUri)
        currentStages[stageIndex] = currentStages[stageIndex].copy(puzzles = currentPuzzles)
        stages.value = currentStages
    }

    fun removeStage(stageIndex: Int) {
        val current = stages.value.toMutableList()
        current.removeAt(stageIndex)
        stages.value = current
    }

    fun saveRoom() {
        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            val room = Room(
                roomId = roomId,
                title = roomTitle.value,
                playerCount = playerCount.value,
                stages = stages.value
            )
            val result = repository.saveRoom(room)
            _saveState.value = if (result.isSuccess) SaveState.Saved
            else SaveState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
        }
    }

    fun loadRoom(roomId: String) {
        viewModelScope.launch {
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val roomDoc = db.collection("rooms").document(roomId).get().await()

                roomTitle.value = roomDoc.getString("title") ?: ""
                playerCount.value = (roomDoc.getLong("playerCount") ?: 2).toInt()
                this@RoomBuilderViewModel.roomId = roomId

                val stagesSnap = db.collection("rooms").document(roomId)
                    .collection("stages").get().await()

                val loadedStages = stagesSnap.documents.sortedBy { it.id }.map { stageDoc ->
                    val puzzlesSnap = db.collection("rooms").document(roomId)
                        .collection("stages").document(stageDoc.id)
                        .collection("puzzles").get().await()

                    val loadedPuzzles = puzzlesSnap.documents.sortedBy { it.id }.map { puzzleDoc ->
                        Puzzle(
                            title = puzzleDoc.getString("title") ?: "",
                            clueText = puzzleDoc.getString("clueText") ?: "",
                            answer = puzzleDoc.getString("answer") ?: "",
                            playerIndex = (puzzleDoc.getLong("playerIndex") ?: 0).toInt(),
                            imageUri = puzzleDoc.getString("imageUri") ?: ""
                        )
                    }
                    Stage(puzzles = loadedPuzzles)
                }
                stages.value = loadedStages
            } catch (e: Exception) { }
        }
    }

    fun resetState() {
        _saveState.value = SaveState.Idle
        roomTitle.value = ""
        playerCount.value = 2
        stages.value = emptyList()
        roomId = ""
    }
}

sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    object Saved : SaveState()
    data class Error(val message: String) : SaveState()
}
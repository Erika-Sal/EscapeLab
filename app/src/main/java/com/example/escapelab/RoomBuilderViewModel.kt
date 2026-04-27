package com.example.escapelab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RoomBuilderViewModel : ViewModel() {
    private val repository = RoomRepository()

    val roomTitle = MutableStateFlow("")
    val playerCount = MutableStateFlow(2)
    val stages = MutableStateFlow<List<Stage>>(emptyList())

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    fun updateTitle(title: String) { roomTitle.value = title }
    val timeLimit = MutableStateFlow(600) // default 10 minutes

    fun updateTimeLimit(seconds: Int) {
        timeLimit.value = seconds
    }
    fun updatePlayerCount(count: Int) {
        playerCount.value = count
        // update existing stages to match new player count
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
                title = roomTitle.value,
                playerCount = playerCount.value,
                timeLimitSeconds = timeLimit.value,
                stages = stages.value
            )
            val result = repository.saveRoom(room)
            _saveState.value = if (result.isSuccess) SaveState.Saved
            else SaveState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
        }
    }

    fun resetState() {
        _saveState.value = SaveState.Idle
        roomTitle.value = ""
        playerCount.value = 2
        timeLimit.value = 600
        stages.value = emptyList()
    }
}

sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    object Saved : SaveState()
    data class Error(val message: String) : SaveState()
}
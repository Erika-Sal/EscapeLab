package com.example.escapelab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RoomBuilderViewModel : ViewModel() {
    private val repository = RoomRepository()

    val roomTitle = MutableStateFlow("")
    val stages = MutableStateFlow<List<Stage>>(emptyList())

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    fun updateTitle(title: String) {
        roomTitle.value = title
    }

    fun addStage() {
        val current = stages.value.toMutableList()
        current.add(Stage(
            stageNumber = current.size,
            puzzles = listOf(
                Puzzle(playerIndex = 0),
                Puzzle(playerIndex = 1)
            )
        ))
        stages.value = current
    }

    fun updatePuzzle(stageIndex: Int, puzzleIndex: Int, puzzle: Puzzle) {
        val currentStages = stages.value.toMutableList()
        val currentPuzzles = currentStages[stageIndex].puzzles.toMutableList()
        currentPuzzles[puzzleIndex] = puzzle
        currentStages[stageIndex] = currentStages[stageIndex].copy(puzzles = currentPuzzles)
        stages.value = currentStages
    }

    fun saveRoom() {
        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            val room = Room(
                title = roomTitle.value,
                stages = stages.value
            )
            val result = repository.saveRoom(room)
            _saveState.value = if (result.isSuccess) SaveState.Saved else SaveState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
        }
    }

    fun resetState() {
        _saveState.value = SaveState.Idle
        roomTitle.value = ""
        stages.value = emptyList()
    }
}

sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    object Saved : SaveState()
    data class Error(val message: String) : SaveState()
}
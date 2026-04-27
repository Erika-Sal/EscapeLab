package com.example.escapelab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LobbyViewModel : ViewModel() {
    private val repository = SessionRepository()

    val sessionCode = MutableStateFlow("")
    val players = MutableStateFlow<List<Player>>(emptyList())
    val session = MutableStateFlow<GameSession?>(null)
    val joinCode = MutableStateFlow("")
    val errorMessage = MutableStateFlow("")
    val isLoading = MutableStateFlow(false)

    fun updateJoinCode(code: String) { joinCode.value = code.uppercase() }

    fun createSession(roomId: String, playerCount: Int) {
        viewModelScope.launch {
            isLoading.value = true
            val result = repository.createSession(roomId, playerCount)
            if (result.isSuccess) {
                val code = result.getOrNull()!!
                sessionCode.value = code
                listenToSession(code)
                listenToPlayers(code)
            } else {
                errorMessage.value = result.exceptionOrNull()?.message ?: "Error"
            }
            isLoading.value = false
        }
    }

    fun joinSession() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = ""
            val result = repository.joinSession(joinCode.value)
            if (result.isSuccess) {
                val code = result.getOrNull()!!
                sessionCode.value = code
                listenToSession(code)
                listenToPlayers(code)
            } else {
                errorMessage.value = result.exceptionOrNull()?.message ?: "Error"
            }
            isLoading.value = false
        }
    }

    fun startGame() {
        viewModelScope.launch {
            repository.setTimer(sessionCode.value, 600)
            repository.startGame(sessionCode.value)
        }
    }
    private fun listenToSession(code: String) {
        viewModelScope.launch {
            repository.listenToSession(code).collect {
                session.value = it
            }
        }
    }

    private fun listenToPlayers(code: String) {
        viewModelScope.launch {
            repository.listenToPlayers(code).collect {
                players.value = it
            }
        }
    }

    fun resetSession() {
        sessionCode.value = ""
        players.value = emptyList()
        session.value = null
        joinCode.value = ""
        errorMessage.value = ""
        isLoading.value = false
    }

    fun getCurrentUserId() = repository.getCurrentUserId()
}
package com.example.escapelab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GameViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val repository = SessionRepository()

    val session = MutableStateFlow<GameSession?>(null)
    val players = MutableStateFlow<List<Player>>(emptyList())
    val myPuzzle = MutableStateFlow<Puzzle?>(null)
    val answerInput = MutableStateFlow("")
    val answerResult = MutableStateFlow<AnswerResult>(AnswerResult.Idle)
    val isLoading = MutableStateFlow(false)

    val chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatInput = MutableStateFlow("")



    private var sessionCode = ""
    private var roomId = ""

    fun init(code: String) {
        sessionCode = code
        listenToSession()
        listenToPlayers()
        listenToChat()
    }

    private fun listenToSession() {
        viewModelScope.launch {
            repository.listenToSession(sessionCode).collect { s ->
                session.value = s
                if (s != null && roomId.isEmpty()) {
                    roomId = s.roomId
                    loadMyPuzzle(s.currentStage)
                } else if (s != null && s.currentStage != lastLoadedStage) {
                    loadMyPuzzle(s.currentStage)
                }
            }
        }
    }

    private fun listenToPlayers() {
        viewModelScope.launch {
            repository.listenToPlayers(sessionCode).collect {
                players.value = it
            }
        }
    }

    private var lastLoadedStage = -1

    private fun loadMyPuzzle(stageIndex: Int) {
        if (stageIndex == lastLoadedStage) return
        lastLoadedStage = stageIndex

        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch

                val playersSnapshot = db.collection("sessions")
                    .document(sessionCode)
                    .collection("players")
                    .get().await()

                val playerIds = playersSnapshot.documents.map { it.id }.sorted()
                val myIndex = playerIds.indexOf(userId)
                if (myIndex == -1) return@launch

                val puzzleDoc = db.collection("rooms")
                    .document(roomId)
                    .collection("stages")
                    .document("stage_$stageIndex")
                    .collection("puzzles")
                    .document("puzzle_$myIndex")
                    .get().await()

                if (puzzleDoc.exists()) {
                    myPuzzle.value = Puzzle(
                        title = puzzleDoc.getString("title") ?: "",
                        clueText = puzzleDoc.getString("clueText") ?: "",
                        answer = puzzleDoc.getString("answer") ?: "",
                        playerIndex = myIndex
                    )
                    answerInput.value = ""
                    answerResult.value = AnswerResult.Idle
                }
            } catch (e: Exception) { }
        }
    }

    fun updateAnswer(input: String) {
        answerInput.value = input
    }

    fun submitAnswer() {
        viewModelScope.launch {
            val puzzle = myPuzzle.value ?: return@launch
            val userId = auth.currentUser?.uid ?: return@launch

            val correct = answerInput.value.trim()
                .equals(puzzle.answer.trim(), ignoreCase = true)

            if (!correct) {
                answerResult.value = AnswerResult.Wrong
                return@launch
            }

            answerResult.value = AnswerResult.Correct

            db.collection("sessions").document(sessionCode)
                .collection("players").document(userId)
                .update("hasSubmittedCorrect", true).await()

            checkStageGate()
        }
    }

    private suspend fun checkStageGate() {
        try {
            val sessionRef = db.collection("sessions").document(sessionCode)

            db.runTransaction { transaction ->
                val sessionSnap = transaction.get(sessionRef)
                val currentStage = sessionSnap.getLong("currentStage")?.toInt() ?: 0
                val playerCount = sessionSnap.getLong("playerCount")?.toInt() ?: 2

                val playersSnap = db.collection("sessions").document(sessionCode)
                    .collection("players").get()

                transaction.update(sessionRef, "stageCheckPending", true)
            }.await()

            val playersSnap = db.collection("sessions").document(sessionCode)
                .collection("players").get().await()

            val allSolved = playersSnap.documents.all {
                it.getBoolean("hasSubmittedCorrect") == true
            }

            if (allSolved) {
                val currentStage = session.value?.currentStage ?: 0
                val playerCount = session.value?.playerCount ?: 2

                val batch = db.batch()
                playersSnap.documents.forEach { doc ->
                    batch.update(doc.reference, "hasSubmittedCorrect", false)
                }

                val sessionRef = db.collection("sessions").document(sessionCode)
                val nextStage = currentStage + 1

                val stagesSnap = db.collection("rooms").document(roomId)
                    .collection("stages").get().await()

                if (nextStage >= stagesSnap.size()) {
                    batch.update(sessionRef, "status", "boss")
                } else {
                    batch.update(sessionRef, "currentStage", nextStage)
                }

                batch.commit().await()
            }
        } catch (e: Exception) {
        }
    }

    fun getCurrentUserId() = auth.currentUser?.uid ?: ""

    fun updateChatInput(input: String) {
        chatInput.value = input
    }

    fun sendMessage() {
        viewModelScope.launch {
            val message = chatInput.value.trim()
            if (message.isEmpty()) return@launch
            chatInput.value = ""
            repository.sendMessage(sessionCode, message)
        }
    }

    private fun listenToChat() {
        viewModelScope.launch {
            repository.listenToChat(sessionCode).collect {
                chatMessages.value = it
            }
        }
    }


}

sealed class AnswerResult {
    object Idle : AnswerResult()
    object Correct : AnswerResult()
    object Wrong : AnswerResult()
}
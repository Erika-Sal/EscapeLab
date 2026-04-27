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

    val timeRemainingSeconds = MutableStateFlow<Int?>(null)
    val timerExpired = MutableStateFlow(false)

    private var sessionCode = ""
    private var roomId = ""

    fun init(code: String) {
        sessionCode = code
        listenToSession()
        listenToPlayers()
        listenToChat()
        listenToTimer()
    }

    private fun listenToSession() {
        viewModelScope.launch {
            repository.listenToSession(sessionCode).collect { s ->
                session.value = s
                if (s != null && roomId.isEmpty()) {
                    roomId = s.roomId
                    loadMyPuzzle(s.currentStage)
                } else if (s != null && s.currentStage != lastLoadedStage) {
                    // Only reload puzzle if stage changed
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
        // Don't reload if we already loaded this stage
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
                    // Only reset answer when moving to a NEW stage
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

            // Mark this player as solved in Firestore
            db.collection("sessions").document(sessionCode)
                .collection("players").document(userId)
                .update("hasSubmittedCorrect", true).await()

            // Check if ALL players have solved — if so advance stage
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

                // Get all players
                val playersSnap = db.collection("sessions").document(sessionCode)
                    .collection("players").get()

                // We check this outside transaction since subcollection reads
                // aren't supported inside transactions
                transaction.update(sessionRef, "stageCheckPending", true)
            }.await()

            // Do the actual check outside transaction
            val playersSnap = db.collection("sessions").document(sessionCode)
                .collection("players").get().await()

            val allSolved = playersSnap.documents.all {
                it.getBoolean("hasSubmittedCorrect") == true
            }

            if (allSolved) {
                val currentStage = session.value?.currentStage ?: 0
                val playerCount = session.value?.playerCount ?: 2

                // Reset all players and advance stage
                val batch = db.batch()
                playersSnap.documents.forEach { doc ->
                    batch.update(doc.reference, "hasSubmittedCorrect", false)
                }

                val sessionRef = db.collection("sessions").document(sessionCode)
                val nextStage = currentStage + 1

                // Check if this was the last stage — go to boss
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
            // stage gate check failed
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


    private fun listenToTimer() {
        viewModelScope.launch {
            var endMs: Long? = null

            while (endMs == null) {
                try {
                    val beforeRead = System.currentTimeMillis()
                    val sessionDoc = db.collection("sessions")
                        .document(sessionCode).get().await()
                    val afterRead = System.currentTimeMillis()

                    // Estimate when the server actually wrote this
                    // by accounting for half the round trip time
                    val networkLatency = (afterRead - beforeRead) / 2

                    endMs = sessionDoc.getLong("timerEndMs")
                    if (endMs != null) {
                        // Adjust endMs to account for network delay
                        // Don't adjust — the endMs is absolute server time
                        // just note how long the read took
                    } else {
                        kotlinx.coroutines.delay(500)
                    }
                } catch (e: Exception) {
                    kotlinx.coroutines.delay(500)
                }
            }

            while (true) {
                val remaining = ((endMs!! - System.currentTimeMillis()) / 1000).toInt()
                when {
                    remaining <= 0 -> {
                        timeRemainingSeconds.value = 0
                        timerExpired.value = true
                        break
                    }
                    else -> timeRemainingSeconds.value = remaining
                }
                kotlinx.coroutines.delay(100)
            }
        }
    }
}

sealed class AnswerResult {
    object Idle : AnswerResult()
    object Correct : AnswerResult()
    object Wrong : AnswerResult()
}
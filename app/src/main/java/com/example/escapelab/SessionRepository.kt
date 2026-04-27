package com.example.escapelab

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Query
class SessionRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun generateCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..4).map { chars.random() }.joinToString("")
    }

    suspend fun createSession(roomId: String, playerCount: Int): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
            val email = auth.currentUser?.email ?: "Host"
            val code = generateCode()

            val player = hashMapOf(
                "userId" to userId,
                "displayName" to email.substringBefore("@"),
                "hasSubmittedCorrect" to false,
                "bossButtonTaps" to 0,
                "bossTargetTaps" to 0
            )

            val session = hashMapOf(
                "sessionCode" to code,
                "roomId" to roomId,
                "hostId" to userId,
                "status" to "waiting",
                "currentStage" to 0,
                "playerCount" to playerCount
            )

            db.collection("sessions").document(code).set(session).await()
            db.collection("sessions").document(code)
                .collection("players").document(userId).set(player).await()

            Result.success(code)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinSession(code: String): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
            val email = auth.currentUser?.email ?: "Player"

            val sessionDoc = db.collection("sessions").document(code).get().await()
            if (!sessionDoc.exists()) return Result.failure(Exception("Room not found!"))

            val status = sessionDoc.getString("status")
            if (status != "waiting") return Result.failure(Exception("Game already started!"))

            val player = hashMapOf(
                "userId" to userId,
                "displayName" to email.substringBefore("@"),
                "hasSubmittedCorrect" to false,
                "bossButtonTaps" to 0,
                "bossTargetTaps" to 0
            )

            db.collection("sessions").document(code)
                .collection("players").document(userId).set(player).await()

            Result.success(code)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun listenToSession(code: String): Flow<GameSession?> = callbackFlow {
        var playersListener: ListenerRegistration? = null

        val sessionListener = db.collection("sessions").document(code)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }

                val session = GameSession(
                    sessionCode = snapshot.getString("sessionCode") ?: code,
                    roomId = snapshot.getString("roomId") ?: "",
                    hostId = snapshot.getString("hostId") ?: "",
                    status = snapshot.getString("status") ?: "waiting",
                    currentStage = (snapshot.getLong("currentStage") ?: 0).toInt(),
                    playerCount = (snapshot.getLong("playerCount") ?: 2).toInt()
                )
                trySend(session)
            }

        awaitClose {
            sessionListener.remove()
            playersListener?.remove()
        }
    }

    fun listenToPlayers(code: String): Flow<List<Player>> = callbackFlow {
        val listener = db.collection("sessions").document(code)
            .collection("players")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener
                val players = snapshot.documents.mapNotNull { doc ->
                    Player(
                        userId = doc.getString("userId") ?: "",
                        displayName = doc.getString("displayName") ?: "",
                        hasSubmittedCorrect = doc.getBoolean("hasSubmittedCorrect") ?: false,
                        bossButtonTaps = (doc.getLong("bossButtonTaps") ?: 0).toInt(),
                        bossTargetTaps = (doc.getLong("bossTargetTaps") ?: 0).toInt()
                    )
                }
                trySend(players)
            }
        awaitClose { listener.remove() }
    }

    suspend fun startGame(code: String): Result<Unit> {
        return try {
            db.collection("sessions").document(code)
                .update("status", "playing").await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUserId() = auth.currentUser?.uid ?: ""

    suspend fun sendMessage(code: String, message: String) {
        val userId = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email ?: "Player"
        val displayName = email.substringBefore("@")

        val chatMessage = hashMapOf(
            "userId" to userId,
            "displayName" to displayName,
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("sessions").document(code)
            .collection("chat")
            .add(chatMessage).await()
    }

    fun listenToChat(code: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = db.collection("sessions").document(code)
            .collection("chat")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener
                val messages = snapshot.documents.mapNotNull { doc ->
                    ChatMessage(
                        userId = doc.getString("userId") ?: "",
                        displayName = doc.getString("displayName") ?: "",
                        message = doc.getString("message") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                }
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    suspend fun setTimer(code: String, durationSeconds: Int) {
        // Store both the end time AND the server offset
        val clientTime = System.currentTimeMillis()
        val endTime = clientTime + (durationSeconds * 1000L)
        db.collection("sessions").document(code)
            .update(mapOf(
                "timerEndMs" to endTime,
                "timerStartMs" to clientTime,
                "timerDurationMs" to (durationSeconds * 1000L)
            )).await()
    }
}
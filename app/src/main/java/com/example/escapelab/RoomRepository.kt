package com.example.escapelab

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RoomRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun saveRoom(room: Room): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
            val roomRef = db.collection("rooms").document()
            val roomId = roomRef.id
            val roomData = hashMapOf(
                "roomId" to roomId,
                "creatorId" to userId,
                "title" to room.title,
                "playerCount" to room.playerCount,
                "isPublic" to room.isPublic,
            )
            roomRef.set(roomData).await()

            room.stages.forEachIndexed { stageIndex, stage ->
                val stageRef = roomRef.collection("stages").document("stage_$stageIndex")
                stageRef.set(mapOf("stageNumber" to stageIndex)).await()

                stage.puzzles.forEachIndexed { puzzleIndex, puzzle ->
                    val puzzleRef = stageRef.collection("puzzles").document("puzzle_$puzzleIndex")
                    puzzleRef.set(mapOf(
                        "playerIndex" to puzzleIndex,
                        "title" to puzzle.title,
                        "clueText" to puzzle.clueText,
                        "answer" to puzzle.answer
                    )).await()
                }
            }
            Result.success(roomId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
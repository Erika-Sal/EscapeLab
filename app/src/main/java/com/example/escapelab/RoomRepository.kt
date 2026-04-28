package com.example.escapelab

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RoomRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun saveRoom(room: Room): Result<String> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("Not logged in"))

            val roomId = if (room.roomId.isNotEmpty()) room.roomId
            else db.collection("rooms").document().id

            val roomData = hashMapOf(
                "roomId" to roomId,
                "creatorId" to userId,
                "title" to room.title,
                "playerCount" to room.playerCount,
                "isPublic" to room.isPublic
            )

            db.collection("rooms").document(roomId).set(roomData).await()

            val existingStages = db.collection("rooms").document(roomId)
                .collection("stages").get().await()
            existingStages.documents.forEach { it.reference.delete().await() }

            room.stages.forEachIndexed { stageIndex, stage ->
                val stageRef = db.collection("rooms").document(roomId)
                    .collection("stages").document("stage_$stageIndex")
                stageRef.set(mapOf("stageNumber" to stageIndex)).await()

                stage.puzzles.forEachIndexed { puzzleIndex, puzzle ->
                    stageRef.collection("puzzles").document("puzzle_$puzzleIndex")
                        .set(mapOf(
                            "title" to puzzle.title,
                            "clueText" to puzzle.clueText,
                            "answer" to puzzle.answer,
                            "playerIndex" to puzzleIndex,
                            "imageUri" to puzzle.imageUri
                        )).await()
                }
            }
            Result.success(roomId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
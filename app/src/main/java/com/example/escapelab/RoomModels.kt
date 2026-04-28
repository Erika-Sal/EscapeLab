package com.example.escapelab

data class Puzzle(
    val puzzleId: String = "",
    val playerIndex: Int = 0,
    val title: String = "",
    val clueText: String = "",
    val answer: String = "",
    val imageUri: String = ""
)

data class Stage(
    val stageId: String = "",
    val stageNumber: Int = 0,
    val puzzles: List<Puzzle> = emptyList()
)

data class Room(
    val roomId: String = "",
    val creatorId: String = "",
    val title: String = "",
    val playerCount: Int = 2,
    val isPublic: Boolean = false,
    val stages: List<Stage> = emptyList()
)
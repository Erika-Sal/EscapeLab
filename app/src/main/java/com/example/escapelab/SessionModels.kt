package com.example.escapelab

data class Player(
    val userId: String = "",
    val displayName: String = "",
    val hasSubmittedCorrect: Boolean = false,
    val bossButtonTaps: Int = 0,
    val bossTargetTaps: Int = 0,
    val bossDigit: Int = -1,
    val bossClue: String = ""
)

data class GameSession(
    val sessionCode: String = "",
    val roomId: String = "",
    val hostId: String = "",
    val status: String = "waiting", // waiting, playing, boss, finished
    val currentStage: Int = 0,
    val playerCount: Int = 2,
    val players: Map<String, Player> = emptyMap()
)

data class ChatMessage(
    val userId: String = "",
    val displayName: String = "",
    val message: String = "",
    val timestamp: Long = 0L
)
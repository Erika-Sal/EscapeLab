package com.example.escapelab

data class DigitClue(
    val digit: Int,
    val clue: String
)

val bossClues = listOf(
    // 0
    DigitClue(0, "The number of lives a cat has... minus nine"),
    DigitClue(0, "What remains when you take everything from nothing"),
    DigitClue(0, "The score of a game not yet begun"),

    // 1
    DigitClue(1, "The loneliest number"),
    DigitClue(1, "How many moons orbit the Earth"),
    DigitClue(1, "The number of heads on a coin"),

    // 2
    DigitClue(2, "Eyes on a face, wings on a bird"),
    DigitClue(2, "The number of halves in a whole"),
    DigitClue(2, "Twins make this many"),

    // 3
    DigitClue(3, "Sides on the shape Pythagoras loved most"),
    DigitClue(3, "A baker's dozen minus ten"),
    DigitClue(3, "Wishes granted by a genie"),

    // 4
    DigitClue(4, "Seasons in a year, legs on a horse"),
    DigitClue(4, "Corners of a perfect room"),
    DigitClue(4, "The number of suits in a deck"),

    // 5
    DigitClue(5, "Fingers on one hand"),
    DigitClue(5, "Points on a sheriff's star"),
    DigitClue(5, "The number of senses"),

    // 6
    DigitClue(6, "Sides of a honeycomb cell"),
    DigitClue(6, "Half a dozen"),
    DigitClue(6, "The highest face on a standard die"),

    // 7
    DigitClue(7, "Days the world was made, colors in a rainbow"),
    DigitClue(7, "The number of deadly sins"),
    DigitClue(7, "Lucky number, wonders of the ancient world"),

    // 8
    DigitClue(8, "Legs on a spider, tentacles on an octopus"),
    DigitClue(8, "Planets in our solar system"),
    DigitClue(8, "The number that looks like infinity standing up"),

    // 9
    DigitClue(9, "Lives of a cat"),
    DigitClue(9, "The square of three"),
    DigitClue(9, "Players on a baseball team")
)

fun getClueForDigit(digit: Int): String {
    val options = bossClues.filter { it.digit == digit }
    return options.random().clue
}

fun generateBossCode(playerCount: Int): List<Int> {
    return (0 until playerCount).map { (0..9).random() }
}
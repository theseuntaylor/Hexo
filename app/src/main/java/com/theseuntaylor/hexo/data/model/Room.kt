package com.theseuntaylor.hexo.data.model

data class Room(
    val roomId: String = "",
    val creatorUsername: String = "",
    val player1Name: String = "",
    val player2Name: String = "",
    val board: List<String> = List(9) { "" },
    val currentTurn: String = "X",
    val status: String = "waiting",
    val winner: String = "",
    val isPublic: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

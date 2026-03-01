package com.theseuntaylor.hexo.data.model

data class Room(
    val roomId: String = "",
    val creatorUsername: String = "",
    val isPublic: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

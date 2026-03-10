package com.theseuntaylor.hexo.data.repository

import com.theseuntaylor.hexo.data.model.Room
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    suspend fun createRoom(room: Room): Result<Room>
    suspend fun joinRoom(roomId: String, playerName: String): Result<Room>
    suspend fun makeMove(roomId: String, cellIndex: Int, symbol: String): Result<Unit>
    suspend fun resetGame(roomId: String): Result<Unit>
    suspend fun setPlayerConnected(roomId: String, symbol: String, connected: Boolean): Result<Unit>
    fun observeRoom(roomId: String): Flow<Room>
}

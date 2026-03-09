package com.theseuntaylor.hexo.data.repository

import com.theseuntaylor.hexo.data.model.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.util.UUID
import javax.inject.Inject

class LocalRoomRepository @Inject constructor() : RoomRepository {

    override suspend fun createRoom(room: Room): Result<Room> {
        return try {
            val roomWithId = room.copy(
                roomId = UUID.randomUUID().toString().take(6).uppercase(),
                player1Name = room.creatorUsername,
                status = "waiting",
            )
            Result.success(roomWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun joinRoom(roomId: String, playerName: String): Result<Room> =
        Result.failure(UnsupportedOperationException("Local repository does not support joining rooms."))

    override suspend fun makeMove(roomId: String, cellIndex: Int, symbol: String): Result<Unit> =
        Result.failure(UnsupportedOperationException("Local repository does not support online moves."))

    override suspend fun resetGame(roomId: String): Result<Unit> =
        Result.failure(UnsupportedOperationException("Local repository does not support online game reset."))

    override fun observeRoom(roomId: String): Flow<Room> = emptyFlow()
}

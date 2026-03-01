package com.theseuntaylor.hexo.data.repository

import com.theseuntaylor.hexo.data.model.Room
import java.util.UUID
import javax.inject.Inject

class LocalRoomRepository @Inject constructor() : RoomRepository {

    override suspend fun createRoom(room: Room): Result<Room> {
        return try {
            val roomWithId = room.copy(roomId = UUID.randomUUID().toString().take(8))
            Result.success(roomWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

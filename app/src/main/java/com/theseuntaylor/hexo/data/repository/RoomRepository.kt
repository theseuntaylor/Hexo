package com.theseuntaylor.hexo.data.repository

import com.theseuntaylor.hexo.data.model.Room

interface RoomRepository {
    suspend fun createRoom(room: Room): Result<Room>
}

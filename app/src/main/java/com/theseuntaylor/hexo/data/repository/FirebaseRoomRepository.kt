package com.theseuntaylor.hexo.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.theseuntaylor.hexo.data.model.Room
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseRoomRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : RoomRepository {

    companion object {
        private const val ROOMS_COLLECTION = "rooms"
    }

    override suspend fun createRoom(room: Room): Result<Room> {
        return try {
            val documentRef = firestore.collection(ROOMS_COLLECTION).document()
            val roomWithId = room.copy(roomId = documentRef.id)

            documentRef.set(roomWithId).await()

            Result.success(roomWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

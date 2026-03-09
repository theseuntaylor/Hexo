package com.theseuntaylor.hexo.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.theseuntaylor.hexo.data.model.Room
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseRoomRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : RoomRepository {

    companion object {
        private const val ROOMS_COLLECTION = "rooms"
        private const val CODE_LENGTH = 6
        private const val CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    }

    private fun generateRoomCode(): String =
        (1..CODE_LENGTH).map { CODE_CHARS.random() }.joinToString("")

    override suspend fun createRoom(room: Room): Result<Room> {
        return try {
            val roomCode = generateRoomCode()
            val documentRef = firestore.collection(ROOMS_COLLECTION).document(roomCode)
            val roomWithId = room.copy(
                roomId = roomCode,
                player1Name = room.creatorUsername,
                status = "waiting",
                currentTurn = "X",
                board = List(9) { "" },
                winner = "",
            )
            documentRef.set(roomWithId).await()
            Result.success(roomWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun joinRoom(roomId: String, playerName: String): Result<Room> {
        return try {
            val documentRef = firestore.collection(ROOMS_COLLECTION).document(roomId)
            val snapshot = documentRef.get().await()

            if (!snapshot.exists()) {
                return Result.failure(Exception("Room '$roomId' not found."))
            }

            val room = snapshot.toObject(Room::class.java)
                ?: return Result.failure(Exception("Failed to parse room data."))

            if (room.status != "waiting") {
                return Result.failure(Exception("Room is no longer available to join."))
            }

            if (room.player2Name.isNotEmpty()) {
                return Result.failure(Exception("Room is already full."))
            }

            val updatedRoom = room.copy(player2Name = playerName, status = "playing")
            documentRef.set(updatedRoom).await()
            Result.success(updatedRoom)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun makeMove(roomId: String, cellIndex: Int, symbol: String): Result<Unit> {
        return try {
            val documentRef = firestore.collection(ROOMS_COLLECTION).document(roomId)
            val snapshot = documentRef.get().await()
            val room = snapshot.toObject(Room::class.java)
                ?: return Result.failure(Exception("Room not found."))

            if (room.board[cellIndex].isNotEmpty()) {
                return Result.failure(Exception("Cell already occupied."))
            }

            val newBoard = room.board.toMutableList().also { it[cellIndex] = symbol }
            val nextTurn = if (symbol == "X") "O" else "X"

            // Check win or draw
            val winner = checkWinner(newBoard, symbol)
            val isFull = newBoard.none { it.isEmpty() }
            val newStatus = when {
                winner != null -> "finished"
                isFull -> "finished"
                else -> "playing"
            }
            val newWinner = when {
                winner != null -> symbol
                isFull -> "draw"
                else -> ""
            }

            documentRef.update(
                mapOf(
                    "board" to newBoard,
                    "currentTurn" to nextTurn,
                    "status" to newStatus,
                    "winner" to newWinner,
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetGame(roomId: String): Result<Unit> {
        return try {
            firestore.collection(ROOMS_COLLECTION).document(roomId).update(
                mapOf(
                    "board" to List(9) { "" },
                    "currentTurn" to "X",
                    "status" to "playing",
                    "winner" to "",
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeRoom(roomId: String): Flow<Room> = callbackFlow {
        val listenerRegistration = firestore
            .collection(ROOMS_COLLECTION)
            .document(roomId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val room = snapshot?.toObject(Room::class.java)
                if (room != null) {
                    trySend(room)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    private val winningLines = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // rows
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // columns
        listOf(0, 4, 8), listOf(2, 4, 6),                   // diagonals
    )

    private fun checkWinner(board: List<String>, symbol: String): String? =
        winningLines.firstOrNull { (a, b, c) ->
            board[a] == symbol && board[b] == symbol && board[c] == symbol
        }?.let { symbol }
}

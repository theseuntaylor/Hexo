package com.theseuntaylor.hexo.feature.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theseuntaylor.hexo.data.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class GameViewModel @Inject constructor(
    @Named("firebase") private val repository: RoomRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val roomId: String = checkNotNull(savedStateHandle["roomId"])
    private val mySymbol: String = checkNotNull(savedStateHandle["mySymbol"])

    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Loading)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        markConnected(connected = true)
        observeRoom()
    }

    // ── Online ──────────────────────────────────────────────────────────────

    private fun observeRoom() {
        viewModelScope.launch {
            repository.observeRoom(roomId)
                .catch { e -> _uiState.value = GameUiState.Error(e.message ?: "Connection error") }
                .collect { room ->
                    val opponentConnected = if (mySymbol == "X") room.player2Connected else room.player1Connected
                    _uiState.value = when {
                        room.status == "waiting" -> GameUiState.WaitingForOpponent(roomId)
                        room.status == "finished" -> GameUiState.Finished(room, mySymbol)
                        !opponentConnected && room.status == "playing" ->
                            GameUiState.OpponentDisconnected(room, mySymbol)
                        else -> GameUiState.InProgress(room, mySymbol)
                    }
                }
        }
    }

    fun makeMove(cellIndex: Int) {
        val state = _uiState.value
        if (state !is GameUiState.InProgress) return
        val room = state.room
        if (room.currentTurn != mySymbol) return
        if (room.board[cellIndex].isNotEmpty()) return

        viewModelScope.launch {
            repository.makeMove(roomId, cellIndex, mySymbol)
                .onFailure { e -> _uiState.value = GameUiState.Error(e.message ?: "Failed to make move") }
        }
    }

    fun resetGame() {
        viewModelScope.launch {
            repository.resetGame(roomId)
                .onFailure { e -> _uiState.value = GameUiState.Error(e.message ?: "Failed to reset game") }
        }
    }

    private fun markConnected(connected: Boolean) {
        viewModelScope.launch {
            repository.setPlayerConnected(roomId, mySymbol, connected)
        }
    }

    override fun onCleared() {
        super.onCleared()
        markConnected(connected = false)
    }
}

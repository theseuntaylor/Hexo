package com.theseuntaylor.hexo.feature.join_room

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theseuntaylor.hexo.data.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class JoinRoomViewModel @Inject constructor(
    @Named("firebase") private val repository: RoomRepository,
) : ViewModel() {

    var uiState by mutableStateOf<JoinRoomUiState>(JoinRoomUiState.Idle)
        private set

    fun joinRoom(roomId: String, playerName: String) {
        uiState = JoinRoomUiState.Loading
        viewModelScope.launch {
            repository.joinRoom(roomId.trim().uppercase(), playerName.trim())
                .onSuccess { room -> uiState = JoinRoomUiState.Success(room.roomId) }
                .onFailure { e -> uiState = JoinRoomUiState.Error(e.message ?: "Failed to join room") }
        }
    }

    fun resetState() {
        uiState = JoinRoomUiState.Idle
    }
}

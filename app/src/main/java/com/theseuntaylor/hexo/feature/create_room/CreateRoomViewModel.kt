package com.theseuntaylor.hexo.feature.create_room

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theseuntaylor.hexo.data.model.Room
import com.theseuntaylor.hexo.data.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class CreateRoomViewModel @Inject constructor(
    @Named("firebase") private val repository: RoomRepository,
) : ViewModel() {

    var uiState by mutableStateOf<CreateRoomUiState>(CreateRoomUiState.Idle)
        private set

    fun createRoom(username: String) {
        uiState = CreateRoomUiState.Loading
        viewModelScope.launch {
            repository.createRoom(Room(creatorUsername = username.trim()))
                .onSuccess { room -> uiState = CreateRoomUiState.Success(room) }
                .onFailure { e -> uiState = CreateRoomUiState.Error(e.message ?: "Failed to create room") }
        }
    }

    fun resetState() {
        uiState = CreateRoomUiState.Idle
    }
}

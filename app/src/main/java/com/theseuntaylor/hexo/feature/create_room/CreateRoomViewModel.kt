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
    @Named("firebase") private val firebaseRoomRepository: RoomRepository,
    @Named("local") private val localRoomRepository: RoomRepository,
) : ViewModel() {

    var isPublic by mutableStateOf(false)
        private set

    var uiState by mutableStateOf<CreateRoomUiState>(CreateRoomUiState.Idle)
        private set

    fun onIsPublicChanged(value: Boolean) {
        isPublic = value
    }

    fun createRoom(username: String) {
        val room = Room(
            creatorUsername = username.trim(),
            isPublic = isPublic,
        )

        val repository = if (isPublic) firebaseRoomRepository else localRoomRepository

        uiState = CreateRoomUiState.Loading

        viewModelScope.launch {
            repository.createRoom(room)
                .onSuccess { createdRoom ->
                    uiState = CreateRoomUiState.Success(createdRoom)
                }
                .onFailure { exception ->
                    uiState = CreateRoomUiState.Error(
                        exception.message ?: "Failed to create room"
                    )
                }
        }
    }

    fun resetState() {
        uiState = CreateRoomUiState.Idle
    }
}

package com.theseuntaylor.hexo.feature.create_room

import com.theseuntaylor.hexo.data.model.Room

sealed class CreateRoomUiState {
    data object Idle : CreateRoomUiState()
    data object Loading : CreateRoomUiState()
    data class Success(val room: Room) : CreateRoomUiState()
    data class Error(val message: String) : CreateRoomUiState()
}

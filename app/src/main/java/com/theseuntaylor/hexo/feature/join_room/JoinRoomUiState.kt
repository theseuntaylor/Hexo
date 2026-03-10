package com.theseuntaylor.hexo.feature.join_room

sealed class JoinRoomUiState {
    data object Idle : JoinRoomUiState()
    data object Loading : JoinRoomUiState()
    data class Success(val roomId: String) : JoinRoomUiState()
    data class Error(val message: String) : JoinRoomUiState()
}

package com.theseuntaylor.hexo.feature.game

import com.theseuntaylor.hexo.data.model.Room

sealed class GameUiState {
    // Shared
    data object Loading : GameUiState()
    data class Error(val message: String) : GameUiState()

    // Online states
    data class WaitingForOpponent(val roomId: String) : GameUiState()
    data class InProgress(val room: Room, val mySymbol: String) : GameUiState()
    data class Finished(val room: Room, val mySymbol: String) : GameUiState()
    data class OpponentDisconnected(val room: Room, val mySymbol: String) : GameUiState()

    // Offline states
    data class OfflineInProgress(val gameState: com.theseuntaylor.hexo.feature.game.GameState) : GameUiState()
    data class OfflineFinished(val gameState: com.theseuntaylor.hexo.feature.game.GameState) : GameUiState()
}

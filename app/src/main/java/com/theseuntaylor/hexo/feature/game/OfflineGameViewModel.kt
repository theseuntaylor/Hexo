package com.theseuntaylor.hexo.feature.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class OfflineGameViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val player1Name: String = savedStateHandle["player1Name"] ?: "Player 1"
    private val player2Name: String = savedStateHandle["player2Name"] ?: "Player 2"

    private val _uiState = MutableStateFlow<GameUiState>(
        GameUiState.OfflineInProgress(
            GameState(
                player1Name = player1Name,
                player2Name = player2Name,
                player1Symbol = CellValue.X,
                player2Symbol = CellValue.O,
            )
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun makeMove(cellIndex: Int) {
        val state = _uiState.value as? GameUiState.OfflineInProgress ?: return
        val gameState = state.gameState
        if (!gameState.isValidMove(cellIndex)) return

        val newBoard = TicTacToeLogic.makeMove(gameState.board, cellIndex, gameState.currentPlayer)
        val newStatus = TicTacToeLogic.determineGameStatus(
            newBoard,
            gameState.currentPlayer,
            gameState.getCurrentPlayerName()
        )
        val updated = gameState.copy(
            board = newBoard,
            gameStatus = newStatus,
            currentPlayer = if (newStatus == GameStatus.InProgress)
                TicTacToeLogic.getNextPlayer(gameState.currentPlayer)
            else
                gameState.currentPlayer
        )
        _uiState.value = if (newStatus == GameStatus.InProgress)
            GameUiState.OfflineInProgress(updated)
        else
            GameUiState.OfflineFinished(updated)
    }

    fun resetGame() {
        _uiState.value = GameUiState.OfflineInProgress(
            GameState(
                player1Name = player1Name,
                player2Name = player2Name,
                player1Symbol = CellValue.X,
                player2Symbol = CellValue.O,
            )
        )
    }
}

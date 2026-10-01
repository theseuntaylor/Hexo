package com.theseuntaylor.hexo.feature.computer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.theseuntaylor.hexo.feature.game.CellValue
import com.theseuntaylor.hexo.feature.game.TicTacToeLogic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

enum class ComputerGameResult { HUMAN_WON, COMPUTER_WON, DRAW }

data class ComputerGameUiState(
    val difficulty: Difficulty,
    val board: List<CellValue> = List(9) { CellValue.Empty },
    val isComputerThinking: Boolean = false,
    val result: ComputerGameResult? = null,
    /** The square the house just took from the human, if it cheated on its last move. */
    val stolenCell: Int? = null,
)

@HiltViewModel
class ComputerGameViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val difficulty: Difficulty = savedStateHandle.get<String>("difficulty")
        ?.let { name -> Difficulty.entries.firstOrNull { it.name == name } }
        ?: Difficulty.EASY

    private val _uiState = MutableStateFlow(ComputerGameUiState(difficulty))
    val uiState: StateFlow<ComputerGameUiState> = _uiState.asStateFlow()

    private var computerTurn: Job? = null
    private var houseTurn = 0

    init {
        if (difficulty == Difficulty.DEVILISH) {
            viewModelScope.launch(Dispatchers.Default) { ComputerOpponent.warmUp() }
        }
    }

    fun onCellClick(index: Int) {
        val state = _uiState.value
        if (state.result != null || state.isComputerThinking || state.board[index] != CellValue.Empty) return

        val board = TicTacToeLogic.makeMove(state.board, index, CellValue.X)
        // A full board isn't a draw against Devilish: the house can still overwrite a square.
        val result = resultOf(board, houseCanStillMove = difficulty == Difficulty.DEVILISH)
        _uiState.value = state.copy(
            board = board,
            result = result,
            isComputerThinking = result == null,
            stolenCell = null,
        )
        if (result == null) playComputerTurn(board)
    }

    fun playAgain() {
        computerTurn?.cancel()
        houseTurn = 0
        _uiState.value = ComputerGameUiState(difficulty)
    }

    private fun playComputerTurn(board: List<CellValue>) {
        computerTurn = viewModelScope.launch {
            delay(COMPUTER_MOVE_DELAY_MS.milliseconds)
            houseTurn++
            val move = withContext(Dispatchers.Default) {
                ComputerOpponent.chooseMove(board, difficulty, houseTurn = houseTurn)
            }
            val newBoard = TicTacToeLogic.makeMove(board, move.index, CellValue.O)
            _uiState.value = _uiState.value.copy(
                board = newBoard,
                result = resultOf(newBoard),
                isComputerThinking = false,
                stolenCell = move.index.takeIf { move.overwrites },
            )
        }
    }

    private fun resultOf(board: List<CellValue>, houseCanStillMove: Boolean = false): ComputerGameResult? = when {
        TicTacToeLogic.checkWin(board, CellValue.X) -> ComputerGameResult.HUMAN_WON
        TicTacToeLogic.checkWin(board, CellValue.O) -> ComputerGameResult.COMPUTER_WON
        TicTacToeLogic.isBoardFull(board) && !houseCanStillMove -> ComputerGameResult.DRAW
        else -> null
    }

    private companion object {
        const val COMPUTER_MOVE_DELAY_MS = 600L
    }
}

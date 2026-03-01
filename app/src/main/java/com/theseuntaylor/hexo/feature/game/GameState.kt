package com.theseuntaylor.hexo.feature.game

/**
 * Represents a cell on the tic-tac-toe board
 * Null means empty, X means player 1, O means player 2
 */
sealed class CellValue {
    object Empty : CellValue()
    object X : CellValue()
    object O : CellValue()
}

/**
 * Represents the current state of the tic-tac-toe game
 */
data class GameState(
    val board: List<CellValue> = List(9) { CellValue.Empty }, // 3x3 board flattened to 9 cells
    val currentPlayer: CellValue = CellValue.X, // X always goes first
    val gameStatus: GameStatus = GameStatus.InProgress,
    val player1Name: String = "Player 1",
    val player2Name: String = "Player 2",
    val player1Symbol: CellValue = CellValue.X,
    val player2Symbol: CellValue = CellValue.O,
) {
    /**
     * Check if a move is valid (cell is empty and game is in progress)
     */
    fun isValidMove(index: Int): Boolean {
        return index in 0..8 && board[index] == CellValue.Empty && gameStatus == GameStatus.InProgress
    }

    /**
     * Get the current player's name
     */
    fun getCurrentPlayerName(): String {
        return if (currentPlayer == player1Symbol) player1Name else player2Name
    }

    /**
     * Get the opposing player's symbol
     */
    fun getOpponentSymbol(symbol: CellValue): CellValue {
        return if (symbol == CellValue.X) CellValue.O else CellValue.X
    }
}

/**
 * Represents the status of the game
 */
sealed class GameStatus {
    object InProgress : GameStatus()
    data class Won(val winner: String) : GameStatus() // winner's name
    object Draw : GameStatus()
}

/**
 * Tic-Tac-Toe game logic
 */
object TicTacToeLogic {
    /**
     * Winning combinations (indices on the board)
     */
    private val winningCombinations = listOf(
        // Rows
        listOf(0, 1, 2),
        listOf(3, 4, 5),
        listOf(6, 7, 8),
        // Columns
        listOf(0, 3, 6),
        listOf(1, 4, 7),
        listOf(2, 5, 8),
        // Diagonals
        listOf(0, 4, 8),
        listOf(2, 4, 6)
    )

    /**
     * Check if a player has won
     */
    fun checkWin(board: List<CellValue>, symbol: CellValue): Boolean {
        return winningCombinations.any { combination ->
            combination.all { index -> board[index] == symbol }
        }
    }

    /**
     * Check if the board is full (draw)
     */
    fun isBoardFull(board: List<CellValue>): Boolean {
        return board.all { it != CellValue.Empty }
    }

    /**
     * Determine the game status after a move
     */
    fun determineGameStatus(
        board: List<CellValue>,
        playerSymbol: CellValue,
        playerName: String
    ): GameStatus {
        return when {
            checkWin(board, playerSymbol) -> GameStatus.Won(playerName)
            isBoardFull(board) -> GameStatus.Draw
            else -> GameStatus.InProgress
        }
    }

    /**
     * Make a move on the board
     */
    fun makeMove(
        board: List<CellValue>,
        index: Int,
        symbol: CellValue
    ): List<CellValue> {
        return board.toMutableList().apply {
            this[index] = symbol
        }
    }

    /**
     * Switch to the next player
     */
    fun getNextPlayer(currentPlayer: CellValue): CellValue {
        return if (currentPlayer == CellValue.X) CellValue.O else CellValue.X
    }
}

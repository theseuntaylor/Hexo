package com.theseuntaylor.hexo.feature.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.theseuntaylor.hexo.core.composables.Button
import com.theseuntaylor.hexo.core.composables.VerticalSpacer
import com.theseuntaylor.hexo.core.theme.md_theme_dark_primary
import com.theseuntaylor.hexo.navigation.landingRoute

@Composable
fun GameScreen(
    navController: NavController,
    player1Name: String = "Player 1",
    player2Name: String = "Player 2"
) {
    var gameState by remember {
        mutableStateOf(
            GameState(
                player1Name = player1Name,
                player2Name = player2Name,
                player1Symbol = CellValue.X,
                player2Symbol = CellValue.O
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigate(landingRoute) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                "TIC TAC TOE",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Box(modifier = Modifier.size(40.dp))
        }

        // Game status
        GameStatusSection(gameState)

        VerticalSpacer(height = 30.dp)

        // Game board
        GameBoard(
            gameState = gameState,
            onCellClick = { index ->
                if (gameState.isValidMove(index)) {
                    // Make the move
                    val newBoard = TicTacToeLogic.makeMove(
                        gameState.board,
                        index,
                        gameState.currentPlayer
                    )

                    // Check game status
                    val newStatus = TicTacToeLogic.determineGameStatus(
                        newBoard,
                        gameState.currentPlayer,
                        gameState.getCurrentPlayerName()
                    )

                    // Update game state
                    gameState = gameState.copy(
                        board = newBoard,
                        gameStatus = newStatus,
                        currentPlayer = if (newStatus == GameStatus.InProgress) {
                            TicTacToeLogic.getNextPlayer(gameState.currentPlayer)
                        } else {
                            gameState.currentPlayer
                        }
                    )
                }
            }
        )

        VerticalSpacer(height = 40.dp)

        // Reset button
        if (gameState.gameStatus != GameStatus.InProgress) {
            Button(
                text = "Play Again",
                onClick = {
                    gameState = GameState(
                        player1Name = player1Name,
                        player2Name = player2Name,
                        player1Symbol = CellValue.X,
                        player2Symbol = CellValue.O
                    )
                }
            )
            VerticalSpacer(height = 10.dp)
        }

        Button(
            text = "Back to Menu",
            onClick = { navController.navigate(landingRoute) }
        )
    }
}

@Composable
fun GameStatusSection(gameState: GameState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (gameState.gameStatus) {
            is GameStatus.InProgress -> {
                Text(
                    "${gameState.getCurrentPlayerName()}'s Turn",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = md_theme_dark_primary
                    )
                )
                VerticalSpacer(height = 8.dp)
                Text(
                    "Symbol: ${gameState.currentPlayer.getSymbolString()}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            is GameStatus.Won -> {
                Text(
                    "${gameState.gameStatus.winner} Wins!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = md_theme_dark_primary
                    )
                )
            }

            GameStatus.Draw -> {
                Text(
                    "It's a Draw!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = md_theme_dark_primary
                    )
                )
            }
        }
    }
}

@Composable
fun GameBoard(
    gameState: GameState,
    onCellClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(Color.White),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (row in 0..2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                for (col in 0..2) {
                    val index = row * 3 + col
                    GameCell(
                        cellValue = gameState.board[index],
                        onClick = { onCellClick(index) },
                        isGameActive = gameState.gameStatus == GameStatus.InProgress,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun GameCell(
    cellValue: CellValue,
    onClick: () -> Unit,
    isGameActive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(Color.LightGray)
            .clickable(enabled = isGameActive && cellValue == CellValue.Empty) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = cellValue.getSymbolString(),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            ),
            color = when (cellValue) {
                CellValue.X -> md_theme_dark_primary
                CellValue.O -> Color.White
                CellValue.Empty -> Color.Transparent
            }
        )
    }
}

private fun CellValue.getSymbolString(): String {
    return when (this) {
        CellValue.X -> "X"
        CellValue.O -> "O"
        CellValue.Empty -> ""
    }
}

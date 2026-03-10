package com.theseuntaylor.hexo.feature.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.theseuntaylor.hexo.core.composables.Button
import com.theseuntaylor.hexo.core.composables.VerticalSpacer
import com.theseuntaylor.hexo.core.theme.md_theme_dark_primary
import com.theseuntaylor.hexo.navigation.landingRoute

@Composable
fun OfflineGameScreen(
    navController: NavController,
    viewModel: OfflineGameViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { navController.navigate(landingRoute) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
            Text(
                "TIC TAC TOE",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            )
            Box(modifier = Modifier.size(40.dp))
        }

        when (val state = uiState) {
            is GameUiState.OfflineInProgress ->
                OfflineGameContent(
                    gameState = state.gameState,
                    isFinished = false,
                    onCellClick = { viewModel.makeMove(it) },
                    onPlayAgain = { viewModel.resetGame() },
                    onBackToMenu = { navController.navigate(landingRoute) },
                )

            is GameUiState.OfflineFinished ->
                OfflineGameContent(
                    gameState = state.gameState,
                    isFinished = true,
                    onCellClick = {},
                    onPlayAgain = { viewModel.resetGame() },
                    onBackToMenu = { navController.navigate(landingRoute) },
                )

            else -> Unit // shouldn't happen for offline
        }
    }
}

@Composable
private fun OfflineGameContent(
    gameState: GameState,
    isFinished: Boolean,
    onCellClick: (Int) -> Unit,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit,
) {
    OfflineStatusSection(gameState)
    VerticalSpacer(height = 30.dp)
    OfflineBoard(
        gameState = gameState,
        isInteractive = !isFinished,
        onCellClick = onCellClick,
    )
    VerticalSpacer(height = 40.dp)
    if (isFinished) {
        Button(text = "Play Again", onClick = onPlayAgain)
        VerticalSpacer(height = 10.dp)
    }
    Button(text = "Back to Menu", onClick = onBackToMenu)
}

@Composable
private fun OfflineStatusSection(gameState: GameState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        when (val status = gameState.gameStatus) {
            is GameStatus.InProgress -> {
                Text(
                    "${gameState.getCurrentPlayerName()}'s Turn",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = md_theme_dark_primary,
                    ),
                )
                VerticalSpacer(height = 4.dp)
                Text(
                    "Symbol: ${gameState.currentPlayer.symbolString()}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            is GameStatus.Won -> {
                Text(
                    "${status.winner} Wins!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = md_theme_dark_primary,
                    ),
                )
            }
            is GameStatus.Draw -> {
                Text(
                    "It's a Draw!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = md_theme_dark_primary,
                    ),
                )
            }
        }
    }
}

@Composable
private fun OfflineBoard(
    gameState: GameState,
    isInteractive: Boolean,
    onCellClick: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(Color.White),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        for (row in 0..2) {
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                for (col in 0..2) {
                    val index = row * 3 + col
                    OfflineCell(
                        cellValue = gameState.board[index],
                        onClick = { onCellClick(index) },
                        isInteractive = isInteractive && gameState.board[index] == CellValue.Empty,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun OfflineCell(
    cellValue: CellValue,
    onClick: () -> Unit,
    isInteractive: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(Color.LightGray)
            .clickable(enabled = isInteractive) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = cellValue.symbolString(),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = when (cellValue) {
                CellValue.X -> md_theme_dark_primary
                CellValue.O -> Color.White
                CellValue.Empty -> Color.Transparent
            },
        )
    }
}

private fun CellValue.symbolString() = when (this) {
    CellValue.X -> "X"
    CellValue.O -> "O"
    CellValue.Empty -> ""
}

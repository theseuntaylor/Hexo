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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.theseuntaylor.hexo.core.composables.Button
import com.theseuntaylor.hexo.core.composables.VerticalSpacer
import com.theseuntaylor.hexo.core.theme.md_theme_dark_primary
import com.theseuntaylor.hexo.data.model.Room
import com.theseuntaylor.hexo.navigation.landingRoute

@Composable
fun GameScreen(
    navController: NavController,
    viewModel: GameViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
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

        when (val state = uiState) {
            is GameUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = md_theme_dark_primary)
                }
            }

            is GameUiState.WaitingForOpponent -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = md_theme_dark_primary)
                        VerticalSpacer(height = 24.dp)
                        Text(
                            "Waiting for opponent...",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = md_theme_dark_primary
                            )
                        )
                        VerticalSpacer(height = 12.dp)
                        Text(
                            "Room code:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        VerticalSpacer(height = 4.dp)
                        Text(
                            state.roomId,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 6.sp
                            ),
                            color = md_theme_dark_primary
                        )
                        VerticalSpacer(height = 4.dp)
                        Text(
                            "Share this code with your opponent",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            is GameUiState.InProgress -> {
                OnlineGameContent(
                    room = state.room,
                    mySymbol = state.mySymbol,
                    isFinished = false,
                    onCellClick = { index -> viewModel.makeMove(index) },
                    onPlayAgain = { viewModel.resetGame() },
                    onBackToMenu = { navController.navigate(landingRoute) }
                )
            }

            is GameUiState.Finished -> {
                OnlineGameContent(
                    room = state.room,
                    mySymbol = state.mySymbol,
                    isFinished = true,
                    onCellClick = {},
                    onPlayAgain = { viewModel.resetGame() },
                    onBackToMenu = { navController.navigate(landingRoute) }
                )
            }

            is GameUiState.OpponentDisconnected -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Opponent disconnected",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = md_theme_dark_primary,
                            )
                        )
                        VerticalSpacer(height = 8.dp)
                        Text(
                            "Waiting for them to reconnect...",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                        VerticalSpacer(height = 24.dp)
                        Button(text = "Back to Menu", onClick = { navController.navigate(landingRoute) })
                    }
                }
            }

            is GameUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        VerticalSpacer(height = 20.dp)
                        Button(text = "Back to Menu", onClick = { navController.navigate(landingRoute) })
                    }
                }
            }

            else -> Unit // offline states handled by OfflineGameScreen
        }
    }
}

@Composable
private fun OnlineGameContent(
    room: Room,
    mySymbol: String,
    isFinished: Boolean,
    onCellClick: (Int) -> Unit,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit,
) {
    val isMyTurn = room.currentTurn == mySymbol && !isFinished

    // Status banner
    OnlineGameStatusSection(room = room, mySymbol = mySymbol, isFinished = isFinished)

    VerticalSpacer(height = 30.dp)

    // Board
    OnlineGameBoard(
        board = room.board,
        isInteractive = isMyTurn,
        onCellClick = onCellClick
    )

    VerticalSpacer(height = 40.dp)

    if (isFinished) {
        Button(text = "Play Again", onClick = onPlayAgain)
        VerticalSpacer(height = 10.dp)
    }

    Button(text = "Back to Menu", onClick = onBackToMenu)
}

@Composable
private fun OnlineGameStatusSection(room: Room, mySymbol: String, isFinished: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        when {
            isFinished && room.winner == "draw" -> {
                Text(
                    "It's a Draw!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = md_theme_dark_primary
                    )
                )
            }
            isFinished && room.winner.isNotEmpty() -> {
                val youWon = room.winner == mySymbol
                Text(
                    if (youWon) "You Win!" else "You Lose!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = md_theme_dark_primary
                    )
                )
            }
            room.currentTurn == mySymbol -> {
                Text(
                    "Your Turn",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = md_theme_dark_primary
                    )
                )
                VerticalSpacer(height = 4.dp)
                Text("You are $mySymbol", style = MaterialTheme.typography.bodyMedium)
            }
            else -> {
                Text(
                    "Opponent's Turn",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                VerticalSpacer(height = 4.dp)
                Text("You are $mySymbol", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun OnlineGameBoard(
    board: List<String>,
    isInteractive: Boolean,
    onCellClick: (Int) -> Unit,
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
                    OnlineGameCell(
                        symbol = board[index],
                        onClick = { onCellClick(index) },
                        isInteractive = isInteractive && board[index].isEmpty(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun OnlineGameCell(
    symbol: String,
    onClick: () -> Unit,
    isInteractive: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(Color.LightGray)
            .clickable(enabled = isInteractive) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            ),
            color = when (symbol) {
                "X" -> md_theme_dark_primary
                "O" -> Color.White
                else -> Color.Transparent
            }
        )
    }
}

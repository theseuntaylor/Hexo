package com.theseuntaylor.hexo.feature.computer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.theseuntaylor.hexo.core.composables.Button
import com.theseuntaylor.hexo.core.composables.VerticalSpacer
import com.theseuntaylor.hexo.core.theme.md_theme_dark_primary
import com.theseuntaylor.hexo.feature.game.CellValue
import com.theseuntaylor.hexo.navigation.landingRoute

@Composable
fun ComputerGameScreen(
    navController: NavController,
    viewModel: ComputerGameViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val backToMenu: () -> Unit = { navController.popBackStack(landingRoute, inclusive = false) }

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
            IconButton(onClick = backToMenu) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
            Text(
                "VS ${state.difficulty.label.uppercase()}",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            )
            Box(modifier = Modifier.size(40.dp))
        }

        Text(
            statusText(state),
            modifier = Modifier.testTag("game_status"),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = md_theme_dark_primary,
            ),
        )
        VerticalSpacer(height = 8.dp)
        // Fixed height so the board doesn't jump when the banner appears.
        Box(modifier = Modifier.height(40.dp), contentAlignment = Alignment.Center) {
            if (state.stolenCell != null) {
                Text(
                    "The House overwrote your X. House rules.",
                    modifier = Modifier.testTag("cheat_banner"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }
        }
        VerticalSpacer(height = 12.dp)

        Board(
            board = state.board,
            stolenCell = state.stolenCell,
            isInteractive = state.result == null && !state.isComputerThinking,
            onCellClick = viewModel::onCellClick,
        )
        VerticalSpacer(height = 40.dp)

        if (state.result != null) {
            Button(text = "Play Again", onClick = viewModel::playAgain)
            VerticalSpacer(height = 10.dp)
        }
        Button(text = "Back to Menu", onClick = backToMenu)
    }
}

private fun statusText(state: ComputerGameUiState): String {
    val isDevilish = state.difficulty == Difficulty.DEVILISH
    return when (state.result) {
        ComputerGameResult.HUMAN_WON -> "You Win!"
        ComputerGameResult.COMPUTER_WON -> if (isDevilish) "The House Wins!" else "Computer Wins!"
        ComputerGameResult.DRAW -> "It's a Draw!"
        null -> when {
            !state.isComputerThinking -> "Your Turn"
            isDevilish -> "The House is plotting…"
            else -> "Computer is thinking…"
        }
    }
}

@Composable
private fun Board(
    board: List<CellValue>,
    stolenCell: Int?,
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
                    val cell = board[index]
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(if (index == stolenCell) MaterialTheme.colorScheme.errorContainer else Color.LightGray)
                            .clickable(enabled = isInteractive && cell == CellValue.Empty) { onCellClick(index) }
                            .testTag("cell_$index"),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = when (cell) {
                                CellValue.X -> "X"
                                CellValue.O -> "O"
                                CellValue.Empty -> ""
                            },
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = when (cell) {
                                CellValue.X -> md_theme_dark_primary
                                CellValue.O -> Color.White
                                CellValue.Empty -> Color.Transparent
                            },
                        )
                    }
                }
            }
        }
    }
}

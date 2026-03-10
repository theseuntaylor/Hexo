package com.theseuntaylor.hexo.feature.join_room

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.theseuntaylor.hexo.R
import com.theseuntaylor.hexo.core.TextFieldState
import com.theseuntaylor.hexo.core.composables.Button
import com.theseuntaylor.hexo.core.composables.HexoTextField
import com.theseuntaylor.hexo.core.composables.Loader
import com.theseuntaylor.hexo.core.composables.VerticalSpacer
import com.theseuntaylor.hexo.core.theme.md_theme_dark_primary
import com.theseuntaylor.hexo.navigation.gameRoute

@Composable
fun JoinRoomScreen(
    navController: NavController,
    viewModel: JoinRoomViewModel = hiltViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState = viewModel.uiState

    val usernameState = remember {
        TextFieldState(
            validator = { it.isNotBlank() },
            errorFor = { "Username is required" }
        )
    }
    val gameIdState = remember {
        TextFieldState(
            validator = { it.isNotBlank() && it.length == 6 },
            errorFor = { "Enter the 6-character room code" }
        )
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is JoinRoomUiState.Success -> {
                val roomId = uiState.roomId
                viewModel.resetState()
                // Joiner is always O
                navController.navigate("$gameRoute/$roomId/O")
            }
            is JoinRoomUiState.Error -> {
                snackbarHostState.showSnackbar(uiState.message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Join",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
            )
            Text(
                "Room",
                style = MaterialTheme.typography.displaySmall,
                color = md_theme_dark_primary,
            )
            VerticalSpacer(height = 40.dp)
            HexoTextField(
                label = R.string.choose_a_username,
                textFieldState = usernameState
            )
            VerticalSpacer(height = 20.dp)
            HexoTextField(
                label = R.string.enter_game_id,
                textFieldState = gameIdState
            )
            VerticalSpacer(height = 4.dp)
            Text(
                stringResource(id = R.string.room_id_obtained),
                style = MaterialTheme.typography.bodySmall,
            )
            VerticalSpacer(height = 20.dp)

            if (uiState is JoinRoomUiState.Loading) {
                Loader()
            } else {
                Button(
                    text = "Join Room",
                    onClick = {
                        usernameState.isFocusedDirty = true
                        gameIdState.isFocusedDirty = true
                        usernameState.enableShowErrors()
                        gameIdState.enableShowErrors()
                        if (usernameState.isValid && gameIdState.isValid) {
                            viewModel.joinRoom(gameIdState.text, usernameState.text)
                        }
                    }
                )
            }
        }
    }
}

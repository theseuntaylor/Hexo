package com.theseuntaylor.hexo.feature.create_room

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
fun CreateRoomScreen(
    navController: NavController,
    viewModel: CreateRoomViewModel = hiltViewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState = viewModel.uiState

    val usernameState = remember {
        TextFieldState(
            validator = { it.isNotBlank() && it.length >= 3 },
            errorFor = { "Username must be at least 3 characters" }
        )
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is CreateRoomUiState.Success -> {
                val room = uiState.room
                viewModel.resetState()
                // Creator is always X; navigate with roomId and symbol
                navController.navigate("$gameRoute/${room.roomId}/X")
            }
            is CreateRoomUiState.Error -> {
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
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                "Create",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
            )
            Text(
                "Room",
                style = MaterialTheme.typography.displaySmall,
                color = md_theme_dark_primary
            )
            VerticalSpacer(height = 40.dp)

            HexoTextField(
                label = R.string.choose_a_username,
                textFieldState = usernameState,
            )

            VerticalSpacer(height = 40.dp)

            if (uiState is CreateRoomUiState.Loading) {
                Loader()
            } else {
                Button(
                    text = "Create Room",
                    onClick = {
                        usernameState.isFocusedDirty = true
                        usernameState.enableShowErrors()
                        if (usernameState.isValid) {
                            viewModel.createRoom(usernameState.text)
                        }
                    },
                )
            }
        }
    }
}

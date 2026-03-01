package com.theseuntaylor.hexo.feature.join_room

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.theseuntaylor.hexo.R
import com.theseuntaylor.hexo.core.TextFieldState
import com.theseuntaylor.hexo.core.composables.Button
import com.theseuntaylor.hexo.core.composables.HexoTextField
import com.theseuntaylor.hexo.core.composables.VerticalSpacer
import com.theseuntaylor.hexo.core.theme.md_theme_dark_primary
import com.theseuntaylor.hexo.navigation.gameRoute

@Composable
fun JoinRoomScreen(navController: NavController) {
    val usernameState = remember {
        TextFieldState(
            validator = { it.isNotBlank() },
            errorFor = { "Username is required" }
        )
    }
    val gameIdState = remember {
        TextFieldState(
            validator = { it.isNotBlank() },
            errorFor = { "Game ID is required" }
        )
    }

    Column(
        modifier = Modifier.padding(20.dp),
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
        Button(
            text = "Join Room",
            onClick = {
                // Mark as focused so errors show
                usernameState.onFocusChange(true)
                gameIdState.onFocusChange(true)
                usernameState.enableShowErrors()
                gameIdState.enableShowErrors()
                
                if (usernameState.isValid && gameIdState.isValid) {
                    // Navigate to game with player 1 name
                    navController.navigate("$gameRoute/${usernameState.text}/Room Creator")
                }
            }
        )
    }
}

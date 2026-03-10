package com.theseuntaylor.hexo.feature.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.theseuntaylor.hexo.R
import com.theseuntaylor.hexo.core.TextFieldState
import com.theseuntaylor.hexo.core.composables.Button
import com.theseuntaylor.hexo.core.composables.HexoTextField
import com.theseuntaylor.hexo.core.composables.VerticalSpacer
import com.theseuntaylor.hexo.core.theme.md_theme_dark_primary
import com.theseuntaylor.hexo.navigation.offlineGameRoute

@Composable
fun LocalGameScreen(navController: NavController) {
    val player1State = remember {
        TextFieldState(
            validator = { it.isNotBlank() && it.length >= 2 },
            errorFor = { "Name must be at least 2 characters" }
        )
    }
    val player2State = remember {
        TextFieldState(
            validator = { it.isNotBlank() && it.length >= 2 },
            errorFor = { "Name must be at least 2 characters" }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "Player",
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
        )
        Text(
            "Names",
            style = MaterialTheme.typography.displaySmall,
            color = md_theme_dark_primary,
        )
        VerticalSpacer(height = 40.dp)

        HexoTextField(
            label = R.string.player_1_name,
            textFieldState = player1State,
        )
        VerticalSpacer(height = 20.dp)
        HexoTextField(
            label = R.string.player_2_name,
            textFieldState = player2State,
        )
        VerticalSpacer(height = 40.dp)

        Button(
            text = "Start Game",
            onClick = {
                player1State.isFocusedDirty = true
                player2State.isFocusedDirty = true
                player1State.enableShowErrors()
                player2State.enableShowErrors()
                if (player1State.isValid && player2State.isValid) {
                    navController.navigate("$offlineGameRoute/${player1State.text}/${player2State.text}")
                }
            }
        )
    }
}

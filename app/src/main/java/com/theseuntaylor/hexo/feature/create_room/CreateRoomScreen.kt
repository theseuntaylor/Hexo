package com.theseuntaylor.hexo.feature.create_room

import android.util.Log
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
import java.util.UUID

@Composable
fun CreateRoomScreen(navController: NavController) {
    var roomId = remember { "" }
    val usernameState = remember {
        TextFieldState(
            validator = { it.isNotBlank() },
            errorFor = { "Username is required" }
        )
    }

    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.Center
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
            textFieldState = usernameState
        )
        VerticalSpacer(height = 20.dp)
        Text(
            stringResource(id = R.string.room_id_prompt),
            style = MaterialTheme.typography.bodySmall
        )
        VerticalSpacer(height = 20.dp)
        Button(
            text = "Create Room",
            onClick = {
                // Mark as focused so errors show
                usernameState.onFocusChange(true)
                usernameState.enableShowErrors()
                
                if (usernameState.isValid) {
                    // Generate a room ID for demonstration
                    roomId = UUID.randomUUID().toString().take(8).uppercase()
                    Log.d("CreateRoom", "Room created with ID: $roomId, Player: ${usernameState.text}")
                    
                    // Navigate to game with player 1 name
                    navController.navigate("$gameRoute/${usernameState.text}/Opponent")
                }
            }
        )
    }
}

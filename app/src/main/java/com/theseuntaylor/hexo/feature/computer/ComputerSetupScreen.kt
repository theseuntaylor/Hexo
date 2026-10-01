package com.theseuntaylor.hexo.feature.computer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.theseuntaylor.hexo.core.composables.Button
import com.theseuntaylor.hexo.core.composables.VerticalSpacer
import com.theseuntaylor.hexo.core.theme.md_theme_dark_primary
import com.theseuntaylor.hexo.navigation.computerGameRoute

@Composable
fun ComputerSetupScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "Choose Your",
            style = MaterialTheme.typography.displaySmall,
        )
        Text(
            "Opponent",
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
            color = md_theme_dark_primary,
        )
        VerticalSpacer(height = 40.dp)

        Difficulty.entries.forEach { difficulty ->
            Button(
                text = difficulty.label,
                onClick = { navController.navigate("$computerGameRoute/${difficulty.name}") },
            )
            VerticalSpacer(height = 4.dp)
            Text(difficulty.description, style = MaterialTheme.typography.bodyMedium)
            VerticalSpacer(height = 20.dp)
        }
    }
}

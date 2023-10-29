package com.theseuntaylor.hexo.feature.landing

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.theseuntaylor.hexo.R
import com.theseuntaylor.hexo.core.composables.Button
import com.theseuntaylor.hexo.core.composables.VerticalSpacer
import com.theseuntaylor.hexo.core.theme.HexoTheme
import com.theseuntaylor.hexo.navigation.createRoomRoute
import com.theseuntaylor.hexo.navigation.joinRoomRoute

@Composable
fun LandingScreen(navController: NavController) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(20.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.colourful_logo),
            contentDescription = stringResource(id = R.string.content_description),
            modifier = Modifier
                .width(64.dp)
                .height(64.dp)
        )
        VerticalSpacer(height = 10.dp)
        Text(
            text = "HEXO",
            style = MaterialTheme.typography.displayMedium

        )
        VerticalSpacer(height = 40.dp)
        Button(text = "Create A Room", onClick = {
            navController.navigate(createRoomRoute)
        })
        VerticalSpacer(height = 20.dp)
        Button(text = "Join A Room", onClick = {
            navController.navigate(joinRoomRoute)
        })
    }
}
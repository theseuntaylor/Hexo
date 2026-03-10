package com.theseuntaylor.hexo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.theseuntaylor.hexo.core.theme.HexoTheme
import com.theseuntaylor.hexo.navigation.createRoomScreen
import com.theseuntaylor.hexo.navigation.gameScreen
import com.theseuntaylor.hexo.navigation.joinRoomScreen
import com.theseuntaylor.hexo.navigation.landingRoute
import com.theseuntaylor.hexo.navigation.landingScreen
import com.theseuntaylor.hexo.navigation.localGameScreen
import com.theseuntaylor.hexo.navigation.offlineGameScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HexoTheme {
                val navController = rememberNavController()
                val snackBarHostState = remember { SnackbarHostState() }

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold { paddingValues ->
                        NavHost(
                            navController = navController,
                            startDestination = landingRoute,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            landingScreen(snackBarHostState = snackBarHostState, navController = navController)
                            createRoomScreen(navController)
                            joinRoomScreen(navController)
                            gameScreen(navController)
                            localGameScreen(navController)
                            offlineGameScreen(navController)
                        }
                    }
                }
            }
        }
    }
}
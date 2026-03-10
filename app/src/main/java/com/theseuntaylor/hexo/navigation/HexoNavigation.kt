package com.theseuntaylor.hexo.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.theseuntaylor.hexo.feature.create_room.CreateRoomScreen
import com.theseuntaylor.hexo.feature.join_room.JoinRoomScreen
import com.theseuntaylor.hexo.feature.landing.LandingScreen
import com.theseuntaylor.hexo.feature.game.GameScreen
import com.theseuntaylor.hexo.feature.game.LocalGameScreen
import com.theseuntaylor.hexo.feature.game.OfflineGameScreen

const val landingRoute = "landing_route"
const val createRoomRoute = "create_room_route"
const val joinRoomRoute = "join_room_route"
const val gameRoute = "game_route"
const val localGameRoute = "local_game_route"
const val offlineGameRoute = "offline_game_route"

fun NavGraphBuilder.landingScreen(snackBarHostState: SnackbarHostState, navController: NavController) {
    composable(route = landingRoute) {
        LandingScreen(navController = navController)
    }
}

fun NavGraphBuilder.createRoomScreen(navController: NavController) {
    composable(route = createRoomRoute) {
        CreateRoomScreen(navController = navController)
    }
}

fun NavGraphBuilder.joinRoomScreen(navController: NavController) {
    composable(route = joinRoomRoute) {
        JoinRoomScreen(navController = navController)
    }
}

fun NavGraphBuilder.gameScreen(navController: NavController) {
    composable(route = "$gameRoute/{roomId}/{mySymbol}") {
        GameScreen(navController = navController)
    }
}

/** Name entry screen before offline game */
fun NavGraphBuilder.localGameScreen(navController: NavController) {
    composable(route = localGameRoute) {
        LocalGameScreen(navController = navController)
    }
}

/** Offline pass-and-play game screen */
fun NavGraphBuilder.offlineGameScreen(navController: NavController) {
    composable(route = "$offlineGameRoute/{player1Name}/{player2Name}") {
        OfflineGameScreen(navController = navController)
    }
}

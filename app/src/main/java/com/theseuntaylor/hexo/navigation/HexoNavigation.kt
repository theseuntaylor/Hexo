package com.theseuntaylor.hexo.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.theseuntaylor.hexo.feature.create_room.CreateRoomScreen
import com.theseuntaylor.hexo.feature.join_room.JoinRoomScreen
import com.theseuntaylor.hexo.feature.landing.LandingScreen
import com.theseuntaylor.hexo.feature.game.GameScreen

const val landingRoute = "landing_route"
const val createRoomRoute = "create_room_route"
const val joinRoomRoute = "join_room_route"
const val gameRoute = "game_route"

fun String.asGameRoute(player1Name: String, player2Name: String): String {
    return "$gameRoute/$player1Name/$player2Name"
}

fun NavGraphBuilder.landingScreen(snackBarHostState: SnackbarHostState, navController : NavController) {
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
    composable(route = "$gameRoute/{player1Name}/{player2Name}") { backStackEntry ->
        val player1Name = backStackEntry.arguments?.getString("player1Name") ?: "Player 1"
        val player2Name = backStackEntry.arguments?.getString("player2Name") ?: "Player 2"
        GameScreen(
            navController = navController,
            player1Name = player1Name,
            player2Name = player2Name
        )
    }
}

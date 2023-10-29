package com.theseuntaylor.hexo.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.theseuntaylor.hexo.feature.create_room.CreateRoomScreen
import com.theseuntaylor.hexo.feature.join_room.JoinRoomScreen
import com.theseuntaylor.hexo.feature.landing.LandingScreen

const val landingRoute = "landing_route"
const val createRoomRoute = "create_room_route"
const val joinRoomRoute = "join_room_route"

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

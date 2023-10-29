package com.theseuntaylor.hexo.core

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.theseuntaylor.hexo.navigation.createRoomRoute
import com.theseuntaylor.hexo.navigation.joinRoomRoute
import com.theseuntaylor.hexo.navigation.landingRoute

fun NavController.navigateToLanding(navOptions: NavOptions? = null) {
    this.navigate(landingRoute, navOptions)
}
fun NavController.navigateToCreateRoom(navOptions: NavOptions? = null) {
    this.navigate(createRoomRoute, navOptions)
}
fun NavController.navigateToJoinRoom(navOptions: NavOptions? = null) {
    this.navigate(joinRoomRoute, navOptions)
}
package com.igz.composenews.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.igz.composenews.ui.screens.about.AboutScreen
import com.igz.composenews.ui.screens.detail.DetailScreen
import com.igz.composenews.ui.screens.home.HomeScreen

object Routes {
    const val HOME = "home"
    const val DETAIL = "detail/{encodedUrl}"
    const val ABOUT = "about"

    fun detail(encodedUrl: String) = "detail/$encodedUrl"
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onOpenDetail = { encodedUrl -> navController.navigate(Routes.detail(encodedUrl)) },
                onOpenAbout = { navController.navigate(Routes.ABOUT) }
            )
        }
        composable(Routes.DETAIL) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("encodedUrl") ?: ""
            DetailScreen(
                encodedUrl = encodedUrl,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}

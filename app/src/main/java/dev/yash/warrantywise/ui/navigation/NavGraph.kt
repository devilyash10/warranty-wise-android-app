package dev.yash.warrantywise.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.yash.warrantywise.ui.screens.*

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object AddProduct : Screen("add_product?productId={productId}") {
        fun createRoute(productId: String? = null) = 
            if (productId != null) "add_product?productId=$productId" else "add_product"
    }
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
    object ServiceHistory : Screen("service_history/{productId}") {
        fun createRoute(productId: String) = "service_history/$productId"
    }
    object Profile : Screen("profile")
}

@Composable
fun WarrantyWiseNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.Auth.route) {
            AuthScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(
            route = Screen.AddProduct.route,
            arguments = listOf(navArgument("productId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStack ->
            AddProductScreen(
                navController = navController,
                productId = backStack.arguments?.getString("productId")
            )
        }
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStack ->
            ProductDetailScreen(
                navController = navController,
                productId = backStack.arguments?.getString("productId") ?: ""
            )
        }
        composable(
            route = Screen.ServiceHistory.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStack ->
            ServiceHistoryScreen(
                navController = navController,
                productId = backStack.arguments?.getString("productId") ?: ""
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
    }
}

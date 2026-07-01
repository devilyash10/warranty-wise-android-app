package dev.yash.warrantywise.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.yash.warrantywise.ui.navigation.Screen
import dev.yash.warrantywise.ui.theme.*
import dev.yash.warrantywise.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var visible by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(800),
        label = "alpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.6f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(2200)
        val destination = if (authViewModel.isLoggedIn) Screen.Home.route else Screen.Auth.route
        navController.navigate(destination) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeepBlue900, DarkBackground))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale).alpha(alpha)
        ) {
            // Gold shield icon
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(pulseScale)
                    .background(
                        Brush.radialGradient(listOf(Gold, GoldDark)),
                        shape = MaterialTheme.shapes.extraLarge
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = DeepBlue900,
                    modifier = Modifier.size(65.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            Text("WarrantyWise", style = MaterialTheme.typography.displayMedium,
                color = TextOnDark, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(8.dp))

            Text("Smart Warranty Management", style = MaterialTheme.typography.titleMedium,
                color = Gold)

            Spacer(Modifier.height(56.dp))

            CircularProgressIndicator(color = Gold, strokeWidth = 2.dp,
                modifier = Modifier.size(30.dp))
        }
    }
}

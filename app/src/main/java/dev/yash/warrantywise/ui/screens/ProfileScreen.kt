package dev.yash.warrantywise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.yash.warrantywise.ui.navigation.Screen
import dev.yash.warrantywise.ui.theme.*
import dev.yash.warrantywise.viewmodel.AuthViewModel
import dev.yash.warrantywise.viewmodel.ProductViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    productViewModel: ProductViewModel = hiltViewModel()
) {
    val userProfile by authViewModel.userProfile.collectAsState()
    val products by productViewModel.products.collectAsState()

    LaunchedEffect(Unit) { authViewModel.loadUserProfile() }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = { BottomNavBar(navController, Screen.Profile) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(DeepBlue900, DarkBackground)))
                    .padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Gold, GoldDark))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (userProfile?.name?.firstOrNull() ?: "U").toString().uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = DeepBlue900, fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        userProfile?.name ?: "User",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextOnDark, fontWeight = FontWeight.Bold
                    )
                    Text(
                        userProfile?.email ?: authViewModel.userEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Stats section
            Text("Summary", style = MaterialTheme.typography.titleMedium,
                color = TextSecondary, modifier = Modifier.padding(horizontal = 20.dp))

            Spacer(Modifier.height(10.dp))

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileStatCard("Total Products", products.size.toString(), Icons.Filled.Inventory2, Modifier.weight(1f))
                ProfileStatCard("Active", productViewModel.activeCount.toString(), Icons.Filled.CheckCircle, Modifier.weight(1f), SuccessGreenLight)
                ProfileStatCard("Expiring", productViewModel.expiringSoonCount.toString(), Icons.Filled.Warning, Modifier.weight(1f), WarningAmber)
            }

            Spacer(Modifier.height(24.dp))

            // Options
            Text("Account", style = MaterialTheme.typography.titleMedium,
                color = TextSecondary, modifier = Modifier.padding(horizontal = 20.dp))

            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                ProfileMenuItem(Icons.Filled.Home, "Dashboard") {
                    navController.navigate(Screen.Home.route) { launchSingleTop = true }
                }
                Divider(color = DarkOutline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                ProfileMenuItem(Icons.Filled.AddCircle, "Add New Product") {
                    navController.navigate(Screen.AddProduct.route)
                }
                Divider(color = DarkOutline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                ProfileMenuItem(Icons.Filled.Logout, "Sign Out", tint = ErrorRedLight) {
                    authViewModel.logout()
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatCard(
    label: String, value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = Gold
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = valueColor, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall,
                color = valueColor, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: androidx.compose.ui.graphics.Color = TextOnDark,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge, color = tint, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.ChevronRight, null, tint = TextSecondary)
        }
    }
}

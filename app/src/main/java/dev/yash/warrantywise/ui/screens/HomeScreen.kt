package dev.yash.warrantywise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.yash.warrantywise.model.Product
import dev.yash.warrantywise.model.WarrantyStatus
import dev.yash.warrantywise.ui.navigation.Screen
import dev.yash.warrantywise.ui.theme.*
import dev.yash.warrantywise.viewmodel.ProductViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    productViewModel: ProductViewModel = hiltViewModel()
) {
    val products by productViewModel.products.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddProduct.route) },
                containerColor = Gold, contentColor = DeepBlue900,
                shape = CircleShape
            ) { Icon(Icons.Filled.Add, "Add Product", modifier = Modifier.size(28.dp)) }
        },
        bottomBar = { BottomNavBar(navController, Screen.Home) }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = 88.dp),
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(DeepBlue900, DarkBackground)))
                        .padding(horizontal = 20.dp, vertical = 28.dp)
                ) {
                    Column {
                        Text("WarrantyWise", style = MaterialTheme.typography.headlineMedium,
                            color = Gold, fontWeight = FontWeight.Bold)
                        Text("Your product dashboard", style = MaterialTheme.typography.bodyMedium,
                            color = TextOnDark.copy(alpha = 0.7f))
                    }
                }
            }

            // Stats row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard("Active", productViewModel.activeCount.toString(), SuccessGreenLight, Modifier.weight(1f))
                    StatCard("Expiring", productViewModel.expiringSoonCount.toString(), WarningAmber, Modifier.weight(1f))
                    StatCard("Expired", productViewModel.expiredCount.toString(), ErrorRedLight, Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        productViewModel.setSearchQuery(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search products or brands...", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Filled.Search, null, tint = Gold) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { 
                                searchQuery = ""
                                productViewModel.setSearchQuery("")
                            }) {
                                Icon(Icons.Filled.Close, null, tint = TextSecondary)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = DarkOutline,
                        focusedTextColor = TextOnDark,
                        unfocusedTextColor = TextOnDark,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    singleLine = true
                )
            }

            // Product list or empty state
            if (products.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Inventory2, null, tint = DarkOutline, modifier = Modifier.size(72.dp))
                            Spacer(Modifier.height(16.dp))
                            Text(if (searchQuery.isEmpty()) "No products yet" else "No matching products", color = TextSecondary,
                                style = MaterialTheme.typography.titleMedium)
                            if (searchQuery.isEmpty()) {
                                Text("Tap + to add your first product", color = DarkOutline,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            } else {
                item {
                    Text(if (searchQuery.isEmpty()) "Your Products" else "Search Results", 
                        style = MaterialTheme.typography.titleMedium,
                        color = TextOnDark, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                }
                items(products, key = { it.productId }) { product ->
                    ProductCard(
                        product = product,
                        dateFormatter = sdf,
                        onClick = { navController.navigate(Screen.ProductDetail.createRoute(product.productId)) },
                        onDelete = { productViewModel.deleteProduct(product.productId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium,
                color = color, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    dateFormatter: SimpleDateFormat,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val (statusColor, statusLabel) = when (product.warrantyStatus) {
        WarrantyStatus.ACTIVE -> SuccessGreenLight to "Active"
        WarrantyStatus.EXPIRING_SOON -> WarningAmber to "Expiring Soon"
        WarrantyStatus.EXPIRED -> ErrorRedLight to "Expired"
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = DarkSurface,
            title = { Text("Delete Product?", color = TextOnDark) },
            text = { Text("This will permanently remove ${product.productName}.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Delete", color = ErrorRedLight)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel", color = DeepBlue300) }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(DeepBlue800),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Devices, null, tint = Gold, modifier = Modifier.size(26.dp))
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(product.productName, style = MaterialTheme.typography.titleMedium,
                    color = TextOnDark, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(product.brand, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(statusLabel, style = MaterialTheme.typography.labelSmall, color = statusColor)
                    }
                    if (product.warrantyStatus != WarrantyStatus.EXPIRED) {
                        val days = product.daysUntilExpiry
                        Text("${days}d left", style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary)
                    }
                }
            }

            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Filled.DeleteOutline, null, tint = ErrorRedLight.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController, current: Screen) {
    NavigationBar(containerColor = DarkSurface, tonalElevation = 8.dp) {
        NavigationBarItem(
            selected = current == Screen.Home,
            onClick = { if (current != Screen.Home) navController.navigate(Screen.Home.route) { launchSingleTop = true } },
            icon = { Icon(Icons.Filled.Home, "Home") },
            label = { Text("Home") },
            colors = navBarColors()
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Screen.AddProduct.route) },
            icon = { Icon(Icons.Filled.AddCircle, "Add") },
            label = { Text("Add") },
            colors = navBarColors()
        )
        NavigationBarItem(
            selected = current == Screen.Profile,
            onClick = { if (current != Screen.Profile) navController.navigate(Screen.Profile.route) { launchSingleTop = true } },
            icon = { Icon(Icons.Filled.Person, "Profile") },
            label = { Text("Profile") },
            colors = navBarColors()
        )
    }
}

@Composable
private fun navBarColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Gold,
    selectedTextColor = Gold,
    indicatorColor = DeepBlue800,
    unselectedIconColor = TextSecondary,
    unselectedTextColor = TextSecondary
)

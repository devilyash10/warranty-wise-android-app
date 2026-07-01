package dev.yash.warrantywise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import dev.yash.warrantywise.model.WarrantyStatus
import dev.yash.warrantywise.ui.navigation.Screen
import dev.yash.warrantywise.ui.theme.*
import dev.yash.warrantywise.viewmodel.ProductViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: String,
    productViewModel: ProductViewModel = hiltViewModel()
) {
    val product by productViewModel.selectedProduct.collectAsState()
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    LaunchedEffect(productId) { productViewModel.loadProduct(productId) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text(product?.productName ?: "Product Detail", fontWeight = FontWeight.Bold, color = TextOnDark) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null, tint = TextOnDark)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.AddProduct.createRoute(productId)) }) {
                        Icon(Icons.Filled.Edit, "Edit Product", tint = Gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlue900)
            )
        }
    ) { padding ->
        product?.let { p ->
            val (statusColor, statusLabel) = when (p.warrantyStatus) {
                WarrantyStatus.ACTIVE -> SuccessGreenLight to "Warranty Active"
                WarrantyStatus.EXPIRING_SOON -> WarningAmber to "Expiring Soon"
                WarrantyStatus.EXPIRED -> ErrorRedLight to "Warranty Expired"
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Invoice image or placeholder
                if (p.invoiceImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = p.invoiceImageUrl,
                        contentDescription = "Invoice",
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        contentScale = ContentScale.Fit,
                        error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Filled.BrokenImage)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(Brush.verticalGradient(listOf(DeepBlue900, DarkBackground))),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Receipt, null, tint = DarkOutline, modifier = Modifier.size(48.dp))
                            Text("No invoice uploaded", color = DarkOutline,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

                    // Status banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Filled.Shield, null, tint = statusColor, modifier = Modifier.size(32.dp))
                            Column {
                                Text(statusLabel, color = statusColor, fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium)
                                if (p.warrantyStatus != WarrantyStatus.EXPIRED) {
                                    Text("${p.daysUntilExpiry} days remaining",
                                        color = statusColor.copy(0.8f),
                                        style = MaterialTheme.typography.bodySmall)
                                } else {
                                    Text("Expired on ${sdf.format(Date(p.warrantyExpiryDate))}",
                                        color = statusColor.copy(0.8f),
                                        style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    // Info cards
                    DetailCard {
                        DetailRow("Product", p.productName, Icons.Filled.ShoppingBag)
                        HorizontalDivider(color = DarkOutline, thickness = 0.5.dp)
                        DetailRow("Brand", p.brand, Icons.Filled.Business)
                        HorizontalDivider(color = DarkOutline, thickness = 0.5.dp)
                        DetailRow("Category", p.category, Icons.Filled.Category)
                    }

                    DetailCard {
                        DetailRow("Purchase Date", sdf.format(Date(p.purchaseDate)), Icons.Filled.CalendarToday)
                        HorizontalDivider(color = DarkOutline, thickness = 0.5.dp)
                        DetailRow("Warranty Period", "${p.warrantyPeriod} months", Icons.Filled.DateRange)
                        HorizontalDivider(color = DarkOutline, thickness = 0.5.dp)
                        DetailRow("Expiry Date", sdf.format(Date(p.warrantyExpiryDate)), Icons.Filled.EventBusy)
                    }

                    if (p.notes.isNotBlank()) {
                        DetailCard {
                            DetailRow("Notes", p.notes, Icons.Filled.Notes)
                        }
                    }

                    // Service history button
                    Button(
                        onClick = { navController.navigate(Screen.ServiceHistory.createRoute(productId)) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBlue800),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Build, null, tint = Gold)
                        Spacer(Modifier.width(8.dp))
                        Text("Service History", color = Gold, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Gold)
        }
    }
}

@Composable
private fun DetailCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(4.dp), content = content)
    }
}

@Composable
private fun DetailRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = Gold, modifier = Modifier.size(20.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = TextOnDark)
        }
    }
}

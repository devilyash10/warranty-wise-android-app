package dev.yash.warrantywise.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import dev.yash.warrantywise.model.Product
import dev.yash.warrantywise.model.productCategories
import dev.yash.warrantywise.ui.theme.*
import dev.yash.warrantywise.viewmodel.ProductUiState
import dev.yash.warrantywise.viewmodel.ProductViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    navController: NavController,
    productId: String? = null,
    productViewModel: ProductViewModel = hiltViewModel()
) {
    val isEditMode = productId != null
    val selectedProduct by productViewModel.selectedProduct.collectAsState()

    var productName by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Smartphone") }
    var purchaseDateText by remember { mutableStateOf("") }
    var warrantyMonths by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var invoiceUri by remember { mutableStateOf<Uri?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    var purchaseDateMs by remember { mutableStateOf(System.currentTimeMillis()) }

    val uiState by productViewModel.uiState.collectAsState()

    LaunchedEffect(productId) {
        if (productId != null) {
            productViewModel.loadProduct(productId)
        }
    }

    LaunchedEffect(selectedProduct) {
        selectedProduct?.let { p ->
            productName = p.productName
            brand = p.brand
            selectedCategory = p.category
            purchaseDateMs = p.purchaseDate
            purchaseDateText = sdf.format(Date(p.purchaseDate))
            warrantyMonths = p.warrantyPeriod.toString()
            notes = p.notes
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { invoiceUri = it } }

    LaunchedEffect(uiState) {
        if (uiState is ProductUiState.Success) {
            productViewModel.resetState()
            navController.popBackStack()
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = purchaseDateMs)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        purchaseDateMs = it
                        purchaseDateText = sdf.format(Date(it))
                    }
                    showDatePicker = false
                }) { Text("OK", color = Gold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = TextSecondary) }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Product" else "Add Product", fontWeight = FontWeight.Bold, color = TextOnDark) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null, tint = TextOnDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlue900)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Invoice image picker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(MaterialTheme.shapes.large)
                    .border(1.dp, DarkOutline, MaterialTheme.shapes.large)
                    .background(DarkSurface)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (invoiceUri != null) {
                    AsyncImage(model = invoiceUri, contentDescription = "Invoice",
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else if (selectedProduct?.invoiceImageUrl?.isNotBlank() == true) {
                    AsyncImage(model = selectedProduct?.invoiceImageUrl, contentDescription = "Invoice",
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.UploadFile, null, tint = Gold, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Tap to upload invoice", color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Fields
            AddFormField("Product Name", productName, { productName = it }, Icons.Filled.ShoppingBag)
            AddFormField("Brand", brand, { brand = it }, Icons.Filled.Business)

            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    leadingIcon = { Icon(Icons.Filled.Category, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = formFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                    modifier = Modifier.background(DarkSurface)
                ) {
                    productCategories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat, color = TextOnDark) },
                            onClick = { selectedCategory = cat; categoryExpanded = false }
                        )
                    }
                }
            }

            // Purchase date
            OutlinedTextField(
                value = purchaseDateText,
                onValueChange = {},
                label = { Text("Purchase Date") },
                leadingIcon = { Icon(Icons.Filled.CalendarToday, null) },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Filled.EditCalendar, null, tint = Gold)
                    }
                },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Tap calendar to select") },
                colors = formFieldColors()
            )

            // Warranty period
            OutlinedTextField(
                value = warrantyMonths,
                onValueChange = { if (it.all(Char::isDigit)) warrantyMonths = it },
                label = { Text("Warranty Period (months)") },
                leadingIcon = { Icon(Icons.Filled.Shield, null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = formFieldColors()
            )

            AddFormField("Notes (optional)", notes, { notes = it }, Icons.Filled.Notes, singleLine = false)

            errorMsg?.let {
                Text(it, color = ErrorRedLight, style = MaterialTheme.typography.bodySmall)
            }

            // Save button
            Button(
                onClick = {
                    when {
                        productName.isBlank() -> errorMsg = "Product name is required"
                        brand.isBlank() -> errorMsg = "Brand is required"
                        purchaseDateText.isBlank() -> errorMsg = "Please select a purchase date"
                        warrantyMonths.isBlank() || warrantyMonths.toIntOrNull() == null -> errorMsg = "Enter valid warranty months"
                        else -> {
                            errorMsg = null
                            val product = (selectedProduct ?: Product()).copy(
                                productName = productName,
                                brand = brand,
                                category = selectedCategory,
                                purchaseDate = purchaseDateMs,
                                warrantyPeriod = warrantyMonths.toInt(),
                                notes = notes
                            )
                            if (isEditMode) {
                                productViewModel.updateProduct(product, invoiceUri)
                            } else {
                                productViewModel.addProduct(product, invoiceUri)
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DeepBlue900),
                enabled = uiState !is ProductUiState.Loading
            ) {
                if (uiState is ProductUiState.Loading) {
                    CircularProgressIndicator(color = DeepBlue900, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Icon(if (isEditMode) Icons.Filled.Edit else Icons.Filled.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isEditMode) "Update Product" else "Save Product", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AddFormField(
    label: String, value: String, onValueChange: (String) -> Unit,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(leadingIcon, null) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        maxLines = if (singleLine) 1 else 4,
        colors = formFieldColors()
    )
}

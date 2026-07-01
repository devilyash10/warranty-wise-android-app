package dev.yash.warrantywise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.yash.warrantywise.model.ServiceHistory
import dev.yash.warrantywise.ui.theme.*
import dev.yash.warrantywise.viewmodel.ServiceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceHistoryScreen(
    navController: NavController,
    productId: String,
    serviceViewModel: ServiceViewModel = hiltViewModel()
) {
    val history by serviceViewModel.serviceHistory.collectAsState()
    val uiState by serviceViewModel.uiState.collectAsState()
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    var showAddSheet by remember { mutableStateOf(false) }

    LaunchedEffect(productId) { serviceViewModel.loadServiceHistory(productId) }

    if (showAddSheet) {
        AddServiceRecordSheet(
            onDismiss = { showAddSheet = false },
            onSave = { record ->
                serviceViewModel.addServiceRecord(productId, record)
                showAddSheet = false
            }
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Service History", fontWeight = FontWeight.Bold, color = TextOnDark) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, null, tint = TextOnDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlue900)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = Gold, contentColor = DeepBlue900
            ) { Icon(Icons.Filled.Add, "Add Service Record") }
        }
    ) { padding ->
        if (history.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Build, null, tint = DarkOutline, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No service records", color = TextSecondary,
                        style = MaterialTheme.typography.titleMedium)
                    Text("Tap + to add a service entry", color = DarkOutline,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(history, key = { it.serviceId }) { record ->
                    ServiceRecordCard(
                        record = record,
                        sdf = sdf,
                        onDelete = { serviceViewModel.deleteServiceRecord(productId, record.serviceId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceRecordCard(
    record: ServiceHistory,
    sdf: SimpleDateFormat,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(DeepBlue800, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Build, null, tint = Gold, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(record.description, style = MaterialTheme.typography.titleMedium,
                    color = TextOnDark, fontWeight = FontWeight.SemiBold)
                if (record.serviceCenter.isNotBlank()) {
                    Text(record.serviceCenter, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.CalendarToday, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                        Text(sdf.format(Date(record.serviceDate)),
                            style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    if (record.cost > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.CurrencyRupee, null, tint = Gold, modifier = Modifier.size(14.dp))
                            Text(String.format("%.2f", record.cost),
                                style = MaterialTheme.typography.labelSmall, color = Gold)
                        }
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.DeleteOutline, null, tint = ErrorRedLight.copy(alpha = 0.7f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddServiceRecordSheet(
    onDismiss: () -> Unit,
    onSave: (ServiceHistory) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var serviceCenter by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var selectedDateMs by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    var dateText by remember { mutableStateOf(sdf.format(Date(selectedDateMs))) }
    var error by remember { mutableStateOf<String?>(null) }

    if (showDatePicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMs)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let {
                        selectedDateMs = it; dateText = sdf.format(Date(it))
                    }
                    showDatePicker = false
                }) { Text("OK", color = Gold) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = TextSecondary) } }
        ) { DatePicker(state = dpState) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface
    ) {
        Column(
            Modifier.padding(horizontal = 20.dp, vertical = 8.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Add Service Record", style = MaterialTheme.typography.headlineSmall,
                color = TextOnDark, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = description, onValueChange = { description = it },
                label = { Text("Description") },
                leadingIcon = { Icon(Icons.Filled.Description, null) },
                modifier = Modifier.fillMaxWidth(),
                colors = formFieldColors()
            )
            OutlinedTextField(
                value = serviceCenter, onValueChange = { serviceCenter = it },
                label = { Text("Service Center (optional)") },
                leadingIcon = { Icon(Icons.Filled.Store, null) },
                modifier = Modifier.fillMaxWidth(),
                colors = formFieldColors()
            )
            OutlinedTextField(
                value = cost, onValueChange = { cost = it },
                label = { Text("Cost (₹)") },
                leadingIcon = { Icon(Icons.Filled.CurrencyRupee, null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = formFieldColors()
            )
            OutlinedTextField(
                value = dateText, onValueChange = {},
                readOnly = true,
                label = { Text("Service Date") },
                leadingIcon = { Icon(Icons.Filled.CalendarToday, null) },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Filled.EditCalendar, null, tint = Gold)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = formFieldColors()
            )

            error?.let { Text(it, color = ErrorRedLight, style = MaterialTheme.typography.bodySmall) }

            Button(
                onClick = {
                    if (description.isBlank()) { error = "Description is required"; return@Button }
                    onSave(
                        ServiceHistory(
                            description = description,
                            serviceCenter = serviceCenter,
                            cost = cost.toDoubleOrNull() ?: 0.0,
                            serviceDate = selectedDateMs
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DeepBlue900)
            ) { Text("Save Record", fontWeight = FontWeight.Bold) }
        }
    }
}

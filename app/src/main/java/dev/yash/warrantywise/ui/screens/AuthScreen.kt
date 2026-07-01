package dev.yash.warrantywise.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.yash.warrantywise.ui.navigation.Screen
import dev.yash.warrantywise.ui.theme.*
import dev.yash.warrantywise.viewmodel.AuthUiState
import dev.yash.warrantywise.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val uiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.Success -> {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Auth.route) { inclusive = true }
                }
                authViewModel.resetState()
            }
            is AuthUiState.Error -> {
                errorMessage = state.message
                authViewModel.resetState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeepBlue900, DarkBackground)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(72.dp))

            // Logo
            Icon(Icons.Filled.Shield, null, tint = Gold, modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(12.dp))
            Text("WarrantyWise", style = MaterialTheme.typography.headlineLarge,
                color = TextOnDark, fontWeight = FontWeight.Bold)
            Text("Your warranty, organized.", style = MaterialTheme.typography.bodyMedium,
                color = Gold)

            Spacer(Modifier.height(40.dp))

            // Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        if (isLoginMode) "Welcome Back" else "Create Account",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextOnDark, fontWeight = FontWeight.Bold
                    )

                    // Name field (register only)
                    AnimatedVisibility(!isLoginMode) {
                        OutlinedTextField(
                            value = name, onValueChange = { name = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Filled.Person, null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = authFieldColors()
                        )
                    }

                    // Email
                    OutlinedTextField(
                        value = email, onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Filled.Email, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = authFieldColors()
                    )

                    // Password
                    OutlinedTextField(
                        value = password, onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Filled.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    null
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = authFieldColors()
                    )

                    // Error
                    errorMessage?.let {
                        Text(it, color = ErrorRedLight, style = MaterialTheme.typography.bodySmall)
                    }

                    // Submit button
                    Button(
                        onClick = {
                            errorMessage = null
                            if (isLoginMode) authViewModel.login(email, password)
                            else authViewModel.register(name, email, password)
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = DeepBlue900),
                        enabled = uiState !is AuthUiState.Loading
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(color = DeepBlue900, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            Text(if (isLoginMode) "Login" else "Register", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Toggle mode
                    TextButton(
                        onClick = { isLoginMode = !isLoginMode; errorMessage = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (isLoginMode) "Don't have an account? Register" else "Already have an account? Login",
                            color = DeepBlue300, style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Gold,
    unfocusedBorderColor = DarkOutline,
    focusedLabelColor = Gold,
    unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextOnDark,
    unfocusedTextColor = TextOnDark,
    cursorColor = Gold,
    focusedLeadingIconColor = Gold,
    unfocusedLeadingIconColor = TextSecondary
)

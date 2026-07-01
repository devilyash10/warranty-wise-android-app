package dev.yash.warrantywise.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.yash.warrantywise.model.User
import dev.yash.warrantywise.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    val isLoggedIn: Boolean get() = authRepository.isLoggedIn
    val userEmail: String get() = authRepository.currentUserEmail

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.login(email.trim(), password)
            _uiState.value = if (result.isSuccess) AuthUiState.Success
            else AuthUiState.Error(result.exceptionOrNull()?.message ?: "Login failed")
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.register(name.trim(), email.trim(), password)
            _uiState.value = if (result.isSuccess) AuthUiState.Success
            else AuthUiState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState.Idle
        _userProfile.value = null
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _userProfile.value = authRepository.getUserProfile()
        }
    }

    fun resetState() { _uiState.value = AuthUiState.Idle }
}

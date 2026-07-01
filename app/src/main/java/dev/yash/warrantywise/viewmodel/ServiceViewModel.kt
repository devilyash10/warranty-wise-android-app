package dev.yash.warrantywise.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.yash.warrantywise.model.ServiceHistory
import dev.yash.warrantywise.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServiceViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    private val _serviceHistory = MutableStateFlow<List<ServiceHistory>>(emptyList())
    val serviceHistory: StateFlow<List<ServiceHistory>> = _serviceHistory.asStateFlow()

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Idle)
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    fun loadServiceHistory(productId: String) {
        viewModelScope.launch {
            serviceRepository.getServiceHistory(productId).collect { _serviceHistory.value = it }
        }
    }

    fun addServiceRecord(productId: String, record: ServiceHistory) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            val result = serviceRepository.addServiceRecord(productId, record)
            _uiState.value = if (result.isSuccess) ProductUiState.Success
            else ProductUiState.Error(result.exceptionOrNull()?.message ?: "Failed to save record")
        }
    }

    fun deleteServiceRecord(productId: String, serviceId: String) {
        viewModelScope.launch { serviceRepository.deleteServiceRecord(productId, serviceId) }
    }

    fun resetState() { _uiState.value = ProductUiState.Idle }
}

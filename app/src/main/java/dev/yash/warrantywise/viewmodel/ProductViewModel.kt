package dev.yash.warrantywise.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.yash.warrantywise.model.Product
import dev.yash.warrantywise.model.WarrantyStatus
import dev.yash.warrantywise.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProductUiState {
    object Idle : ProductUiState()
    object Loading : ProductUiState()
    object Success : ProductUiState()
    data class Error(val message: String) : ProductUiState()
}

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    
    val products: StateFlow<List<Product>> = _searchQuery
        .combine(_products) { query, list ->
            if (query.isBlank()) list
            else list.filter { 
                it.productName.contains(query, ignoreCase = true) || 
                it.brand.contains(query, ignoreCase = true) 
            }
        }.asStateFlow(viewModelScope, _products.value)

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Idle)
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    val activeCount get() = _products.value.count { it.warrantyStatus == WarrantyStatus.ACTIVE }
    val expiringSoonCount get() = _products.value.count { it.warrantyStatus == WarrantyStatus.EXPIRING_SOON }
    val expiredCount get() = _products.value.count { it.warrantyStatus == WarrantyStatus.EXPIRED }

    init { loadProducts() }

    private fun loadProducts() {
        viewModelScope.launch {
            productRepository.getProducts().collect { _products.value = it }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addProduct(product: Product, invoiceUri: Uri?) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            val result = productRepository.addProduct(product, invoiceUri)
            _uiState.value = if (result.isSuccess) ProductUiState.Success
            else ProductUiState.Error(result.exceptionOrNull()?.message ?: "Failed to add product")
        }
    }

    fun updateProduct(product: Product, newInvoiceUri: Uri?) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            val result = productRepository.updateProduct(product, newInvoiceUri)
            _uiState.value = if (result.isSuccess) ProductUiState.Success
            else ProductUiState.Error(result.exceptionOrNull()?.message ?: "Failed to update product")
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch { productRepository.deleteProduct(productId) }
    }

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            _selectedProduct.value = productRepository.getProduct(productId)
        }
    }

    fun resetState() { 
        _uiState.value = ProductUiState.Idle 
        _selectedProduct.value = null
    }
}

// Extension to help with StateFlow combine
fun <T> kotlinx.coroutines.flow.Flow<T>.asStateFlow(
    scope: kotlinx.coroutines.CoroutineScope,
    initialValue: T
): StateFlow<T> {
    val state = MutableStateFlow(initialValue)
    scope.launch {
        this@asStateFlow.collect { state.value = it }
    }
    return state.asStateFlow()
}

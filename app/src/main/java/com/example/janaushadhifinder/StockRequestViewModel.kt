package com.example.janaushadhifinder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StockRequestUiState(
    val quantity: String = "1",
    val userPhone: String = "",
    val notes: String = "",
    val selectedStoreName: String = sampleStores.firstOrNull()?.name ?: "Nearby Jan-Aushadhi Store",
    val isSending: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = ""
)

class StockRequestViewModel(
    private val repository: StockRequestRepository = StockRequestRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(StockRequestUiState())
    val uiState: StateFlow<StockRequestUiState> = _uiState

    fun prepare(medicine: Medicine) {
        _uiState.value = StockRequestUiState()
    }

    fun onQuantityChanged(value: String) {
        if (value.length <= 3 && value.all { it.isDigit() }) {
            _uiState.update { it.copy(quantity = value, errorMessage = "") }
        }
    }

    fun onPhoneChanged(value: String) {
        if (value.length <= 10 && value.all { it.isDigit() }) {
            _uiState.update { it.copy(userPhone = value, errorMessage = "") }
        }
    }

    fun onNotesChanged(value: String) {
        if (value.length <= 180) {
            _uiState.update { it.copy(notes = value, errorMessage = "") }
        }
    }

    fun onStoreSelected(storeName: String) {
        _uiState.update { it.copy(selectedStoreName = storeName, errorMessage = "") }
    }

    fun clearResult() {
        _uiState.update { it.copy(errorMessage = "", successMessage = "") }
    }

    fun sendRequest(medicine: Medicine) {
        val current = _uiState.value
        val quantity = current.quantity.toIntOrNull()
        val phone = current.userPhone.trim()

        when {
            quantity == null || quantity <= 0 -> {
                _uiState.update { it.copy(errorMessage = "Enter a valid quantity.") }
                return
            }
            phone.isNotBlank() && phone.length != 10 -> {
                _uiState.update { it.copy(errorMessage = "Enter a valid 10-digit phone number or leave it blank.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, errorMessage = "", successMessage = "") }
            val request = StockRequest(
                medicineName = medicine.brandName,
                genericName = medicine.genericName,
                storeName = current.selectedStoreName,
                quantity = quantity,
                userPhone = phone,
                notes = current.notes.trim(),
                status = StockRequestStatus.Pending.value
            )
            val result = repository.sendStockRequest(request)
            _uiState.update {
                it.copy(
                    isSending = false,
                    successMessage = if (result.isSuccess) {
                        "Your stock request has been sent successfully. Store will contact you shortly."
                    } else {
                        ""
                    },
                    errorMessage = result.exceptionOrNull()?.message
                        ?: if (result.isFailure) "Unable to send request. Check internet and retry." else ""
                )
            }
        }
    }
}

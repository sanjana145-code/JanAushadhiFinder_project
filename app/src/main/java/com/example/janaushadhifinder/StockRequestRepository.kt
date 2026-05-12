package com.example.janaushadhifinder

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StockRequestRepository(
    private val firebaseRepository: FirebaseRepository = FirebaseRepository()
) {
    suspend fun sendStockRequest(request: StockRequest): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            firebaseRepository.saveStockRequest(request)
        }
    }
}

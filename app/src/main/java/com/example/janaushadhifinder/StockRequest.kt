package com.example.janaushadhifinder

data class StockRequest(
    val requestId: String = "",
    val medicineName: String = "",
    val genericName: String = "",
    val storeName: String = "",
    val quantity: Int = 1,
    val userPhone: String = "",
    val notes: String = "",
    val status: String = StockRequestStatus.Pending.value,
    val timestampMillis: Long = System.currentTimeMillis()
)

enum class StockRequestStatus(val value: String) {
    Pending("Pending"),
    Accepted("Accepted"),
    OutOfStock("Out of Stock")
}

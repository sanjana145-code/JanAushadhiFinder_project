package com.example.janaushadhifinder

data class Medicine(
    val brandName: String,
    val genericName: String,
    val brandedPrice: Int,
    val genericPrice: Int,
    val manufacturer: String = "Jan-Aushadhi",
    val imageName: String = "pill",
    val category: String = "General"
) {
    val savings: Int get() = brandedPrice - genericPrice
    val savingsPercent: Int
        get() = if (brandedPrice > 0) ((savings.toFloat() / brandedPrice) * 100).toInt() else 0
}

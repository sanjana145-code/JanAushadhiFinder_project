package com.example.janaushadhifinder

data class Medicine(
    val brandName: String,
    val genericName: String,
    val brandedPrice: Int,
    val genericPrice: Int
) {
    val savings: Int get() = brandedPrice - genericPrice
    val savingsPercent: Int get() = ((savings.toFloat() / brandedPrice) * 100).toInt()
}
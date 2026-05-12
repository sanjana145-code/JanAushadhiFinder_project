package com.example.janaushadhifinder.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.janaushadhifinder.Medicine

@Entity(
    tableName = "medicines",
    indices = [
        Index(value = ["brandName"]),
        Index(value = ["genericName"]),
        Index(value = ["manufacturer"]),
        Index(value = ["category"])
    ]
)
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val brandName: String,
    val genericName: String,
    val brandedPrice: Int,
    val genericPrice: Int,
    val manufacturer: String,
    val imageName: String,
    val category: String
) {
    fun toMedicine(): Medicine = Medicine(
        brandName = brandName,
        genericName = genericName,
        brandedPrice = brandedPrice,
        genericPrice = genericPrice,
        manufacturer = manufacturer,
        imageName = imageName,
        category = category
    )
}

fun Medicine.toEntity(): MedicineEntity = MedicineEntity(
    brandName = brandName,
    genericName = genericName,
    brandedPrice = brandedPrice,
    genericPrice = genericPrice,
    manufacturer = manufacturer,
    imageName = imageName,
    category = category
)

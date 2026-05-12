package com.example.janaushadhifinder.data

import android.content.Context
import org.json.JSONArray

class MedicineSeedLoader(private val context: Context) {
    fun load(): List<MedicineEntity> {
        val json = context.assets.open("medicines_seed.json")
            .bufferedReader()
            .use { it.readText() }
        val array = JSONArray(json)
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    MedicineEntity(
                        brandName = item.getString("brandName"),
                        genericName = item.getString("genericName"),
                        brandedPrice = item.getInt("brandPrice"),
                        genericPrice = item.getInt("genericPrice"),
                        manufacturer = item.optString("manufacturer", "Jan-Aushadhi"),
                        imageName = item.optString("imageName", "pill"),
                        category = item.optString("category", "General")
                    )
                )
            }
        }
    }
}

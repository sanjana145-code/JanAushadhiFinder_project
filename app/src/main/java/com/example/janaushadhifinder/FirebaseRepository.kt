package com.example.janaushadhifinder

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseRepository {

    private val db = FirebaseDatabase
        .getInstance("https://janaushadhifinder-3c266-default-rtdb.firebaseio.com")
        .reference

    suspend fun getMedicines(): List<Medicine> = suspendCoroutine { cont ->
        Log.d("Firebase", "Fetching medicines...")
        db.child("medicines").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("Firebase", "Snapshot received. Children count: ${snapshot.childrenCount}")
                    val list = mutableListOf<Medicine>()
                    for (child in snapshot.children) {
                        try {
                            val brandName    = child.child("brand_name").getValue(String::class.java)  ?: ""
                            val genericName  = child.child("generic_name").getValue(String::class.java) ?: ""
                            val brandedPrice = child.child("price_branded").getValue(Long::class.java)?.toInt() ?: 0
                            val genericPrice = child.child("price_generic").getValue(Long::class.java)?.toInt() ?: 0
                            Log.d("Firebase", "Medicine: $brandName -> $genericName")
                            if (brandName.isNotEmpty()) {
                                list.add(Medicine(brandName, genericName, brandedPrice, genericPrice))
                            }
                        } catch (e: Exception) {
                            Log.e("Firebase", "Error parsing medicine: ${e.message}")
                            continue
                        }
                    }
                    Log.d("Firebase", "Total medicines fetched: ${list.size}")
                    cont.resume(list)
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Database error: ${error.message}")
                    cont.resume(emptyList())
                }
            }
        )
    }

    suspend fun getStores(): List<JanAushadhiStore> = suspendCoroutine { cont ->
        Log.d("Firebase", "Fetching stores...")
        db.child("stores").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("Firebase", "Stores snapshot. Children: ${snapshot.childrenCount}")
                    val list = mutableListOf<JanAushadhiStore>()
                    for (child in snapshot.children) {
                        try {
                            val store = JanAushadhiStore(
                                name    = child.child("name").getValue(String::class.java)     ?: "",
                                address = child.child("address").getValue(String::class.java)  ?: "",
                                lat     = child.child("lat").getValue(Double::class.java)      ?: 0.0,
                                lng     = child.child("lng").getValue(Double::class.java)      ?: 0.0,
                                isOpen  = child.child("is_open").getValue(Boolean::class.java) ?: false
                            )
                            if (store.name.isNotEmpty()) list.add(store)
                        } catch (e: Exception) {
                            Log.e("Firebase", "Error parsing store: ${e.message}")
                            continue
                        }
                    }
                    cont.resume(list)
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Stores error: ${error.message}")
                    cont.resume(emptyList())
                }
            }
        )
    }
}
package com.example.janaushadhifinder.data

import android.content.Context
import com.example.janaushadhifinder.FirebaseRepository
import com.example.janaushadhifinder.FuzzySearch
import com.example.janaushadhifinder.Medicine
import com.example.janaushadhifinder.MedicineData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MedicineRepository(
    private val context: Context,
    private val dao: MedicineDao = AppDatabase.getInstance(context).medicineDao(),
    private val firebaseRepository: FirebaseRepository = FirebaseRepository()
) {
    suspend fun seedIfNeeded() = withContext(Dispatchers.IO) {
        if (dao.count() == 0) {
            val seeded = runCatching { MedicineSeedLoader(context).load() }.getOrElse {
                MedicineData.allMedicines.map { medicine -> medicine.toEntity() }
            }
            dao.insertAll(seeded)
        }
    }

    suspend fun syncFirebaseOnce(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val cloudMedicines = firebaseRepository.getMedicines()
            if (cloudMedicines.isNotEmpty()) {
                dao.insertAll(cloudMedicines.map { it.toEntity() })
                true
            } else {
                false
            }
        }.getOrDefault(false)
    }

    fun observeMedicines(query: String): Flow<List<Medicine>> {
        val normalized = query.trim()
        return dao.observeAll().map { rows ->
            val medicines = rows.map { it.toMedicine() }
            if (normalized.isBlank()) {
                medicines
            } else {
                medicines
                    .filter { medicine ->
                        FuzzySearch.matchesAny(
                            normalized,
                            medicine.brandName,
                            medicine.genericName,
                            medicine.category,
                            medicine.manufacturer
                        )
                    }
                    .sortedWith(
                        compareBy<Medicine> {
                            FuzzySearch.medicineScore(
                                normalized,
                                it.brandName,
                                it.genericName,
                                it.category,
                                it.manufacturer
                            )
                        }.thenBy { it.brandName }
                    )
            }
        }.flowOn(Dispatchers.Default)
    }

    fun observeSuggestions(query: String): Flow<List<String>> {
        return dao.observeAll().map { rows ->
            val candidates = rows.flatMap { listOf(it.brandName, it.genericName) }
            FuzzySearch.suggestions(query, candidates)
        }.flowOn(Dispatchers.Default)
    }

    fun observeClosestMatch(query: String): Flow<String?> {
        return dao.observeAll().map { rows ->
            val candidates = rows.flatMap { listOf(it.brandName, it.genericName) }
            FuzzySearch.closest(query, candidates)
        }.flowOn(Dispatchers.Default)
    }
}

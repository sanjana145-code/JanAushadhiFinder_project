package com.example.janaushadhifinder.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {
    @Query("SELECT COUNT(*) FROM medicines")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(medicines: List<MedicineEntity>)

    @Query("SELECT * FROM medicines ORDER BY brandName COLLATE NOCASE")
    fun observeAll(): Flow<List<MedicineEntity>>

    @Query(
        """
        SELECT * FROM medicines
        WHERE brandName LIKE '%' || :query || '%'
           OR genericName LIKE '%' || :query || '%'
           OR category LIKE '%' || :query || '%'
        ORDER BY brandName COLLATE NOCASE
        LIMIT 80
        """
    )
    fun observeSearch(query: String): Flow<List<MedicineEntity>>
}

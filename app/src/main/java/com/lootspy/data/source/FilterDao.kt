package com.lootspy.data.source

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FilterDao {
  @Query("SELECT * FROM filters")
  fun observeAll(): Flow<List<LocalFilter>>

  @Query("SELECT * FROM filters WHERE id = :filterId")
  fun observeById(filterId: String): Flow<LocalFilter>

  @Query("SELECT * FROM filters")
  fun getAll(): List<LocalFilter>

  @Query("SELECT * FROM filters WHERE id = :filterId")
  fun getById(filterId: String): LocalFilter?

  @Upsert
  suspend fun upsert(lootEntry: LocalFilter)

  @Upsert
  suspend fun upsertAll(loot: List<LocalFilter>)

  @Query("DELETE FROM filters WHERE id = :filterId")
  suspend fun deleteById(filterId: String): Int

  @Query("DELETE FROM filters")
  suspend fun deleteAll()
}
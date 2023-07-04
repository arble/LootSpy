package com.lootspy.data.source

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
  @Query("SELECT * FROM profiles")
  fun observeAll(): Flow<List<LocalProfile>>

  @Query("SELECT * FROM profiles WHERE id = :membershipId")
  fun observeById(membershipId: Long): Flow<LocalProfile>

  @Query("SELECT * FROM profiles")
  suspend fun getAll(): List<LocalProfile>

  @Query("SELECT * from profiles WHERE id = :membershipId")
  suspend fun getById(membershipId: Long): LocalProfile?

  @Upsert
  suspend fun upsert(profile: LocalProfile)

  @Upsert
  suspend fun upsertAll(profiles: List<LocalProfile>)

  @Query("DELETE FROM profiles")
  suspend fun deleteAll()
}
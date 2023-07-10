package com.lootspy.data.source

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
  @Query("SELECT * FROM profiles")
  fun observeAll(): Flow<List<DestinyProfile>>

  @Query("SELECT * FROM profiles WHERE membershipId = :membershipId")
  fun observeById(membershipId: Long): Flow<DestinyProfile>

  @Query("SELECT * FROM profiles")
  suspend fun getAll(): List<DestinyProfile>

  @Query("SELECT * from profiles WHERE membershipId = :membershipId")
  suspend fun getById(membershipId: Long): DestinyProfile?

  @Upsert
  suspend fun upsert(profile: DestinyProfile)

  @Upsert
  suspend fun upsertAll(profiles: List<DestinyProfile>)

  @Query("DELETE FROM profiles")
  suspend fun deleteAll()
}
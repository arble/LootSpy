package com.lootspy.data.source

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
  @Query("SELECT * FROM characters")
  fun observeAll(): Flow<List<DestinyCharacter>>

  @Query("SELECT * FROM characters WHERE characterId = :characterId")
  fun observeById(characterId: Long): Flow<DestinyCharacter>

  @Query("SELECT * FROM characters")
  suspend fun getAll(): List<DestinyCharacter>

  @Query("SELECT * from characters WHERE characterId = :characterId")
  suspend fun getById(characterId: Long): DestinyCharacter?

  @Upsert
  suspend fun upsert(character: DestinyCharacter)

  @Upsert
  suspend fun upsertAll(characters: List<DestinyCharacter>)

  @Query("DELETE FROM characters")
  suspend fun deleteAll()
}
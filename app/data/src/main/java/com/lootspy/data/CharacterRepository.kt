package com.lootspy.data

import com.lootspy.data.source.DestinyCharacter
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
  fun getCharactersStream(): Flow<List<DestinyCharacter>>

  suspend fun getCharacters(): List<DestinyCharacter>

  fun getCharacterStream(characterId: Long): Flow<DestinyCharacter?>

  suspend fun getCharacter(characterId: Long): DestinyCharacter?

  suspend fun saveCharacters(characters: List<DestinyCharacter>)
}
package com.lootspy.data

import com.lootspy.data.source.CharacterDao
import com.lootspy.data.source.DestinyCharacter
import com.lootspy.di.ApplicationScope
import com.lootspy.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultCharacterRepository @Inject constructor(
  private val localDataSource: CharacterDao,
  @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
  @ApplicationScope private val scope: CoroutineScope,
) : CharacterRepository {
  override fun getCharactersStream(): Flow<List<DestinyCharacter>> {
    return localDataSource.observeAll()
  }

  override suspend fun getCharacters(): List<DestinyCharacter> {
    return withContext(dispatcher) { localDataSource.getAll() }
  }

  override fun getCharacterStream(characterId: Long): Flow<DestinyCharacter?> {
    return localDataSource.observeById(characterId)
  }

  override suspend fun getCharacter(characterId: Long): DestinyCharacter? {
    return localDataSource.getById(characterId)
  }

  override suspend fun saveCharacters(characters: List<DestinyCharacter>) {
    localDataSource.upsertAll(characters)
  }

}
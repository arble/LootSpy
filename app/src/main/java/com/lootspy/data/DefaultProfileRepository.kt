package com.lootspy.data

import com.lootspy.client.model.DestinyResponsesDestinyProfileUserInfoCard
import com.lootspy.data.source.ProfileDao
import com.lootspy.di.ApplicationScope
import com.lootspy.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultProfileRepository @Inject constructor(
  private val localDataSource: ProfileDao,
  @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
  @ApplicationScope private val scope: CoroutineScope,
) : ProfileRepository {
  override fun getProfilesStream(): Flow<List<DestinyResponsesDestinyProfileUserInfoCard>> {
    return localDataSource.observeAll().map { withContext(dispatcher) { it.toExternal() } }
  }

  override suspend fun getProfiles(): List<DestinyResponsesDestinyProfileUserInfoCard> {
    return withContext(dispatcher) { localDataSource.getAll().toExternal() }
  }

  override fun getProfileStream(membershipId: Long): Flow<DestinyResponsesDestinyProfileUserInfoCard?> {
    return localDataSource.observeById(membershipId).map { it.toExternal() }
  }

  override suspend fun getProfile(membershipId: Long): DestinyResponsesDestinyProfileUserInfoCard? {
    return localDataSource.getById(membershipId)?.toExternal()
  }

  override suspend fun saveProfiles(profiles: List<DestinyResponsesDestinyProfileUserInfoCard>) {
    localDataSource.upsertAll(profiles.toLocal())
  }
}
package com.lootspy.data

import com.lootspy.client.model.DestinyResponsesDestinyProfileUserInfoCard
import com.lootspy.client.model.GroupsV2GroupUserInfoCard
import com.lootspy.data.source.DestinyProfile
import com.lootspy.di.ApplicationScope
import com.lootspy.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultProfileRepository @Inject constructor(
  private val localDataSource: com.lootspy.data.source.ProfileDao,
  @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
  @ApplicationScope private val scope: CoroutineScope,
) : ProfileRepository {
  override fun getProfilesStream(): Flow<List<DestinyProfile>> {
    return localDataSource.observeAll()
  }

  override suspend fun getProfiles(): List<DestinyProfile> {
    return withContext(dispatcher) { localDataSource.getAll() }
  }

  override fun getProfileStream(membershipId: Long): Flow<DestinyProfile?> {
    return localDataSource.observeById(membershipId)
  }

  override suspend fun getProfile(membershipId: Long): DestinyProfile? {
    return localDataSource.getById(membershipId)
  }

  override suspend fun saveProfiles(profiles: List<DestinyProfile>) {
    localDataSource.upsertAll(profiles)
  }
}
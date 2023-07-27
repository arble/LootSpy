package com.lootspy.data.repo

import com.lootspy.data.source.DestinyProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
  fun getProfilesStream(): Flow<List<DestinyProfile>>

  suspend fun getProfiles(): List<DestinyProfile>

  fun getProfileStream(membershipId: Long): Flow<DestinyProfile?>

  suspend fun getProfile(membershipId: Long): DestinyProfile?

  suspend fun saveProfiles(profiles: List<DestinyProfile>)
}
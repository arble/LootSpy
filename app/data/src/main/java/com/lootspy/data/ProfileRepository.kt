package com.lootspy.data

import com.lootspy.client.model.DestinyResponsesDestinyProfileUserInfoCard
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
  fun getProfilesStream(): Flow<List<DestinyResponsesDestinyProfileUserInfoCard>>

  suspend fun getProfiles(): List<DestinyResponsesDestinyProfileUserInfoCard>

  fun getProfileStream(membershipId: Long): Flow<DestinyResponsesDestinyProfileUserInfoCard?>

  suspend fun getProfile(membershipId: Long): DestinyResponsesDestinyProfileUserInfoCard?

  suspend fun saveProfiles(profiles: List<DestinyResponsesDestinyProfileUserInfoCard>)
}
package com.lootspy.data

import com.lootspy.client.model.DestinyResponsesDestinyProfileUserInfoCard
import com.lootspy.client.model.GroupsV2GroupUserInfoCard
import com.lootspy.data.source.DestinyProfile
import com.lootspy.data.source.LocalLootEntry
import kotlinx.serialization.json.Json

private val json = Json {
  isLenient = true
  encodeDefaults = true
  allowStructuredMapKeys = true
}

fun LootEntry.toLocal() = LocalLootEntry(id = id, name = name)

@JvmName("externalToLocalLootEntry")
fun List<LootEntry>.toLocal() = map(LootEntry::toLocal)

fun LocalLootEntry.toExternal() = LootEntry(id = id, name = name)

@JvmName("localToExternalLootEntry")
fun List<LocalLootEntry>.toExternal() = map(LocalLootEntry::toExternal)

fun DestinyResponsesDestinyProfileUserInfoCard.toLocal(): DestinyProfile {
  return DestinyProfile(
    membershipId!!.toLong(),
    membershipType!!,
    displayName!!,
    supplementalDisplayName!!,
    iconPath!!,
    bungieGlobalDisplayName!!,
    bungieGlobalDisplayNameCode!!
  )
}

fun GroupsV2GroupUserInfoCard.toLocal(): DestinyProfile {
  return DestinyProfile(
    membershipId!!.toLong(),
    membershipType!!,
    displayName!!,
    supplementalDisplayName!!,
    iconPath!!,
    bungieGlobalDisplayName!!,
    bungieGlobalDisplayNameCode!!
  )
}

@JvmName("externalToLocalProfile")
fun List<GroupsV2GroupUserInfoCard>.toLocal() =
  map(GroupsV2GroupUserInfoCard::toLocal)
package com.lootspy.data

import com.lootspy.client.model.DestinyResponsesDestinyProfileUserInfoCard
import com.lootspy.client.model.GroupsV2GroupUserInfoCard
import com.lootspy.data.source.DestinyProfile
import com.lootspy.data.source.LocalLootEntry
import com.lootspy.types.item.DestinyItem
import com.lootspy.types.item.LootEntry
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json {
  isLenient = true
  encodeDefaults = true
  allowStructuredMapKeys = true
}

fun LootEntry.toLocal() =
  LocalLootEntry(item.hash.toLong(), Json.encodeToString(item), Json.encodeToString(filterNames))

fun LocalLootEntry.toExternal() =
  LootEntry(Json.decodeFromString(itemData), Json.decodeFromString(filterData))

@JvmName("externalToLocalLootEntry")
fun List<LootEntry>.toLocal() = map(LootEntry::toLocal)

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
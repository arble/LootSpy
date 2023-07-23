package com.lootspy.data

import com.lootspy.client.model.DestinyResponsesDestinyProfileUserInfoCard
import com.lootspy.client.model.GroupsV2GroupUserInfoCard
import com.lootspy.data.source.LocalFilter
import com.lootspy.data.source.LocalLootEntry
import com.lootspy.data.source.DestinyProfile
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
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

fun Filter.toLocal() = LocalFilter(id, name, json.encodeToString(this))

@JvmName("externalToLocalFilter")
fun List<Filter>.toLocal() = map(Filter::toLocal)

fun LocalFilter.toExternal() = Filter(id, name, json.decodeFromString(filterData))

@JvmName("localToExternalFilter")
fun List<LocalFilter>.toExternal() = map(LocalFilter::toExternal)



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

//fun DestinyProfile.toExternal(): GroupsV2GroupUserInfoCard {
//  val result = GroupsV2GroupUserInfoCard()
//  result.membershipId = membershipId
//  result.membershipType = membershipType
//  result.displayName = displayName
//  result.supplementalDisplayName = platformDisplayName
//  result.iconPath = iconPath
//  result.bungieGlobalDisplayName = bungieDisplayName
//  result.bungieGlobalDisplayNameCode = bungieDisplayNameCode
//  return result
//}
//
//@JvmName("localToExternalProfile")
//fun List<DestinyProfile>.toExternal() = map(DestinyProfile::toExternal)
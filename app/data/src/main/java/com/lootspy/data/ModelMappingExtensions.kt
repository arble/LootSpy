package com.lootspy.data

import com.lootspy.client.model.DestinyResponsesDestinyProfileUserInfoCard
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json {
  isLenient = true
  encodeDefaults = true
  allowStructuredMapKeys = true
}

fun LootEntry.toLocal() = com.lootspy.data.source.LocalLootEntry(id = id, name = name)

@JvmName("externalToLocalLootEntry")
fun List<LootEntry>.toLocal() = map(LootEntry::toLocal)

fun com.lootspy.data.source.LocalLootEntry.toExternal() = LootEntry(id = id, name = name)

@JvmName("localToExternalLootEntry")
fun List<com.lootspy.data.source.LocalLootEntry>.toExternal() = map(com.lootspy.data.source.LocalLootEntry::toExternal)

fun Filter.toLocal() = com.lootspy.data.source.LocalFilter(id, name, json.encodeToString(this))

@JvmName("externalToLocalFilter")
fun List<Filter>.toLocal() = map(Filter::toLocal)

fun com.lootspy.data.source.LocalFilter.toExternal() = Filter(id, name, json.decodeFromString(filterData))

@JvmName("localToExternalFilter")
fun List<com.lootspy.data.source.LocalFilter>.toExternal() = map(com.lootspy.data.source.LocalFilter::toExternal)

fun DestinyResponsesDestinyProfileUserInfoCard.toLocal(): com.lootspy.data.source.LocalProfile {
  return com.lootspy.data.source.LocalProfile(
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
fun List<DestinyResponsesDestinyProfileUserInfoCard>.toLocal() =
  map(DestinyResponsesDestinyProfileUserInfoCard::toLocal)

fun com.lootspy.data.source.LocalProfile.toExternal(): DestinyResponsesDestinyProfileUserInfoCard {
  val result = DestinyResponsesDestinyProfileUserInfoCard()
  result.membershipId = id
  result.membershipType = membershipType
  result.displayName = displayName
  result.supplementalDisplayName = platformDisplayName
  result.iconPath = iconPath
  result.bungieGlobalDisplayName = bungieDisplayName
  result.bungieGlobalDisplayNameCode = bungieDisplayNameCode
  return result
}

@JvmName("localToExternalProfile")
fun List<com.lootspy.data.source.LocalProfile>.toExternal() = map(com.lootspy.data.source.LocalProfile::toExternal)
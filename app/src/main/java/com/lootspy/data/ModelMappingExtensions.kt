package com.lootspy.data

import com.lootspy.data.source.LocalFilter
import com.lootspy.data.source.LocalLootEntry
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
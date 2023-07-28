package com.lootspy.filter

import com.lootspy.data.source.LocalFilter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json {
  isLenient = true
  encodeDefaults = true
  allowStructuredMapKeys = true
}

fun Filter.toLocal() = LocalFilter(id, name, json.encodeToString(this))

@JvmName("externalToLocalFilter")
fun List<Filter>.toLocal() = map(Filter::toLocal)

fun LocalFilter.toExternal() =
  Filter(id, name, json.decodeFromString(filterData))

@JvmName("localToExternalFilter")
fun List<LocalFilter>.toExternal() = map(LocalFilter::toExternal)
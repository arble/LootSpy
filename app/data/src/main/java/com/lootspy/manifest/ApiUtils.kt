package com.lootspy.manifest

import android.database.Cursor
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun Cursor.manifestColumns(): Pair<UInt, JsonObject> {
  val idIndex = getColumnIndex("id")
  val jsonIndex = getColumnIndex("json")
  val jsonString =
    getBlob(jsonIndex).toString(Charsets.US_ASCII).let { it.substring(0, it.length - 1) }
  return Pair(getInt(idIndex).toUInt(), Json.decodeFromString(jsonString))
}

fun JsonObject.displayString(prop: String): String? =
  get("displayProperties")?.jsonObject?.get(prop)?.jsonPrimitive?.content


fun Cursor.blobToJson(index: Int): JsonObject {
  val blob = getBlob(index)
  val jsonString = blob.toString(Charsets.US_ASCII).let { it.substring(0, it.length - 1) }
  return Json.decodeFromString(jsonString)
}

fun Cursor.blobToJson(): JsonObject {
  return blobToJson(getColumnIndex("json"))
}

fun JsonObject.displayPair(first: String, second: String): Pair<String, String>? {
  val displayObj = get("displayProperties")?.jsonObject
  if (displayObj != null) {
    val firstProperty = displayObj[first]
    val secondProperty = displayObj[second]
    if (firstProperty != null && secondProperty != null) {
      return Pair(firstProperty.jsonPrimitive.content, secondProperty.jsonPrimitive.content)
    }
  }
  return null
}

fun JsonObject.displayTriple(
  first: String,
  second: String,
  third: String
): Triple<String, String, String>? {
  val displayObj = get("displayProperties")?.jsonObject
  if (displayObj != null) {
    val firstProperty = displayObj[first]
    val secondProperty = displayObj[second]
    val thirdProperty = displayObj[third]
    if (firstProperty != null && secondProperty != null && thirdProperty != null) {
      return Triple(
        firstProperty.jsonPrimitive.content,
        secondProperty.jsonPrimitive.content,
        thirdProperty.jsonPrimitive.content
      )
    }
  }
  return null
}

//fun ApiClient.buildBungieCall(
//  apiPath: String,
//  method: String = "GET",
//  queryParams: List<Pair<String, String>> = listOf(),
//  collectionQueryParams: List<Pair<String, String>> = listOf(),
//  body: Any? = null,
//  headerParams: Map<String, String> = mapOf(),
//  cookieParams: Map<String, String> = mapOf(),
//  formParams: Map<String, Any> = mapOf(),
//  authNames: Array<String> = emptyArray(),
//): Call {
//  val innerHeaderParams = headerParams.toMutableMap()
//  innerHeaderParams["X-API-Key"] = "50ef71cc77324212886181190ea75ba7"
//  return buildCall(
//    "https://www.bungie.net/Platform",
//    apiPath,
//    method,
//    pairsToApiClientPairs(queryParams),
//    pairsToApiClientPairs(collectionQueryParams),
//    body,
//    innerHeaderParams,
//    cookieParams,
//    formParams,
//    authNames,
//    null,
//  )
//}

//fun notifySyncFailure(apiResponse: ApiResponse<out Any>, context: Context, notifyChannel: String) {
//  Log.d(
//    "LootSpy API Sync",
//    "Sync failed due to error code from server: ${apiResponse.statusCode}"
//  )
//  val builder = NotificationCompat.Builder(context, notifyChannel)
//    .setContentTitle("LootSpy sync failed")
//    .setContentText("Couldn't get loot. You may need to log in again.")
//    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//    .setAutoCancel(true)
//    .setTimeoutAfter(5000)
//  with(NotificationManagerCompat.from(context)) {
//    if (ActivityCompat.checkSelfPermission(
//        context,
//        Manifest.permission.POST_NOTIFICATIONS
//      ) == PackageManager.PERMISSION_GRANTED
//    ) {
//      notify(0, builder.build())
//    }
//  }
//}
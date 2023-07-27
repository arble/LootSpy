package com.lootspy.api

class BungiePathHelper {
  companion object {
    fun getFullUrlForPath(path: String?): String {
      return "https://www.bungie.net/$path"
    }
  }
}
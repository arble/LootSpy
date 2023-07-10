package com.lootspy.util

class BungiePathHelper {
  companion object {
    fun getFullUrlForPath(path: String): String {
      return "https://www.bungie.net/$path"
    }
  }
}
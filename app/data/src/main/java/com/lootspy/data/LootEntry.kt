package com.lootspy.data

import com.lootspy.manifest.DestinyItem

data class LootEntry(val item: DestinyItem, val filterNames: Collection<String>)
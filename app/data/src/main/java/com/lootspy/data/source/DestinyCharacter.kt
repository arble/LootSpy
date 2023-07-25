package com.lootspy.data.source

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lootspy.client.model.DestinyEntitiesCharactersDestinyCharacterComponentEmblemColor

fun DestinyEntitiesCharactersDestinyCharacterComponentEmblemColor.toInt(): Int {
  return 0xFF shl 24 or ((red!![0].toInt() and 0xFF) shl 16) or
      ((green!![0].toInt() and 0xFF) shl 8) or
      (blue!![0].toInt() and 0xFF)
}

@Entity(tableName = "characters")
data class DestinyCharacter(
  @PrimaryKey val characterId: Long,
  val membershipId: Long,
  val membershipType: Int,
  val power: Int,
  val race: String,
  val guardianClass: String,
  val emblemPath: String,
  val emblemColor: Int,
) {
  data class Builder(
    val characterId: Long,
    val membershipId: Long,
    val membershipType: Int,
    val power: Int,
    val race: String,
    val guardianClass: String,
    val emblemColor: DestinyEntitiesCharactersDestinyCharacterComponentEmblemColor,
  ) {
    private lateinit var emblemPath: String
    fun emblemPath(emblemPath: String) = apply { this.emblemPath = emblemPath }
    fun build() = DestinyCharacter(
      characterId,
      membershipId,
      membershipType,
      power,
      race,
      guardianClass,
      emblemPath,
      emblemColor.toInt()
    )
  }
}
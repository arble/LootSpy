package com.lootspy.filter.matcher

import com.lootspy.filter.R

enum class MatcherType {
  INVALID {
    override fun printableName() = "INVALID"

    override fun descriptionResource() = R.string.matcher_type_desc_invalid

  },
  NAME {
    override fun printableName() = "Name"
    override fun descriptionResource() = R.string.matcher_type_desc_name
  };

  abstract fun printableName(): String

  abstract fun descriptionResource(): Int

  companion object {
    fun displayValues() = values().filter { it != INVALID }
  }
}
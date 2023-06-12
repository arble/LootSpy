package com.lootspy.util

import com.example.lootspy.R

typealias Validator = Pair<(String) -> Boolean, Int>

class Validation {

  companion object {
    fun validate(text: String, validators: List<Validator>): Int? {
      for ((validator, error) in validators) {
        if (!validator(text)) {
          return error
        }
      }
      return null
    }

    val VALIDATOR_EMPTY: Validator = Pair({ it.isNotEmpty() }, R.string.validator_empty)
    val VALIDATOR_ALPHANUM: Validator =
      Pair({ !it.contains("[^a-zA-Z0-9]".toRegex()) }, R.string.validator_alphanum)
    val VALIDATOR_ALPHANUM_SPACE: Validator =
      Pair({ !it.contains("[^a-zA-Z0-9 ]".toRegex()) }, R.string.validator_alphanum_space)
    val VALIDATOR_LENGTH_32: Validator = Pair({ it.length <= 32 }, R.string.validator_length_32)

    val VALIDATORS_NORMAL_TEXT = listOf(VALIDATOR_EMPTY, VALIDATOR_ALPHANUM_SPACE)
  }
}
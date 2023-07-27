package com.lootspy.api

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import java.util.TreeSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutocompleteHelper @Inject constructor() {
  data class Node(
    var word: Boolean = false,
    val childNodes: MutableMap<Char, Node> = mutableMapOf()
  )

  private val root = Node()
//  val items: MutableMap<String, AutocompleteItem> = HashMap()
  val items = object : HashMap<String, AutocompleteItem>() {
    override fun get(key: String): AutocompleteItem? {
      return super.get(key.toUpperCase(Locale.current))
    }

    override fun putIfAbsent(key: String, value: AutocompleteItem): AutocompleteItem? {
      return super.putIfAbsent(key.toUpperCase(Locale.current), value)
    }

    override fun put(key: String, value: AutocompleteItem): AutocompleteItem? {
      return super.put(key.toUpperCase(Locale.current), value)
    }
  }

  fun insert(item: AutocompleteItem): Boolean {
    if (items.putIfAbsent(item.name.toUpperCase(Locale.current), item) != null) {
      return false
    }
    insertTrie(item.name)
    return true
  }

  fun insertSuccessor(item: AutocompleteItem) {
    items[item.name] = item
  }

  private fun insertTrie(word: String) {
    var currentNode = root
    for (char in word.toUpperCase(Locale.current)) {
      if (currentNode.childNodes[char] == null) {
        currentNode.childNodes[char] = Node()
      }
      currentNode = currentNode.childNodes[char]!!
    }
    currentNode.word = true
  }

  private fun suggestHelper(
    node: Node,
    resultSet: TreeSet<String>,
    current: StringBuilder,
    limit: Int,
  ): Boolean {
    if (node.word) {
      resultSet.add(current.toString())
      if (limit > 0 && resultSet.size >= limit) {
        return false
      }
    }
    if (node.childNodes.isEmpty()) {
      return true
    }
    for (childEntry in node.childNodes) {
      if (!suggestHelper(childEntry.value, resultSet, current.append(childEntry.key), limit)) {
        return false
      }
      current.setLength(current.length - 1)
    }
    return true
  }

  fun suggest(prefix: String, limit: Int = 0): List<AutocompleteItem> {
    val resultNames = TreeSet<String>()
    var leaf = root
    val current = StringBuilder()
    for (ch in prefix.toUpperCase(Locale.current)) {
      leaf = leaf.childNodes[ch] ?: return emptyList()
      current.append(ch)
    }
    suggestHelper(leaf, resultNames, current, limit)
    val result = ArrayList<AutocompleteItem>(resultNames.size)
    resultNames.forEach { name -> items[name]?.let { result.add(it) } }
    return result
  }
}
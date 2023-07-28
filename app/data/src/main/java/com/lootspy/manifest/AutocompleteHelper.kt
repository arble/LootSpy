package com.lootspy.manifest

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.TreeSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutocompleteHelper @Inject constructor(
  @ApplicationContext context: Context,
) {
  data class Node(
    var word: Boolean = false,
    val childNodes: MutableMap<Char, Node> = mutableMapOf()
  )

  private val root = Node()
  private val locale = context.resources.configuration.locales.get(0)
//  val items: MutableMap<String, AutocompleteItem> = HashMap()
  val items = object : HashMap<String, BasicItem>() {
    override fun get(key: String): BasicItem? {
      return super.get(key.uppercase(locale))
    }

    override fun putIfAbsent(key: String, value: BasicItem): BasicItem? {
      return super.putIfAbsent(key.uppercase(locale), value)
    }

    override fun put(key: String, value: BasicItem): BasicItem? {
      return super.put(key.uppercase(locale), value)
    }
  }

  fun insert(item: BasicItem): Boolean {
    if (items.putIfAbsent(item.name.uppercase(locale), item) != null) {
      return false
    }
    insertTrie(item.name)
    return true
  }

  fun insertSuccessor(item: BasicItem) {
    items[item.name] = item
  }

  private fun insertTrie(word: String) {
    var currentNode = root
    for (char in word.uppercase(locale)) {
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

  fun suggest(prefix: String, limit: Int = 0): List<BasicItem> {
    val resultNames = TreeSet<String>()
    var leaf = root
    val current = StringBuilder()
    for (ch in prefix.uppercase(locale)) {
      leaf = leaf.childNodes[ch] ?: return emptyList()
      current.append(ch)
    }
    suggestHelper(leaf, resultNames, current, limit)
    val result = ArrayList<BasicItem>(resultNames.size)
    resultNames.forEach { name -> items[name]?.let { result.add(it) } }
    return result
  }
}
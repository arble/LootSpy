package com.lootspy.api

import java.util.TreeSet
import javax.inject.Singleton

@Singleton
class AutocompleteHelper {
  data class Node(
    var word: Boolean = false,
    val childNodes: MutableMap<Char, Node> = mutableMapOf()
  )

  private val root = Node()
  private val items = HashMap<String, AutocompleteItem>()

  fun insert(item: AutocompleteItem) {
    insertTrie(item.name)
    items[item.name] = item
  }

  private fun insertTrie(word: String) {
    var currentNode = root
    for (char in word) {
      if (currentNode.childNodes[char] == null) {
        currentNode.childNodes[char] = Node()
      }
      currentNode = currentNode.childNodes[char]!!
    }
    currentNode.word = true
  }

  private fun suggestHelper(node: Node, resultSet: TreeSet<String>, current: StringBuilder) {
    if (node.word) {
      resultSet.add(current.toString())
    }
    if (node.childNodes.isEmpty()) {
      return
    }
    for (childEntry in node.childNodes) {
      suggestHelper(childEntry.value, resultSet, current.append(childEntry.key))
      current.setLength(current.length - 1)
    }
  }

  fun suggest(prefix: String): List<AutocompleteItem> {
    val resultNames = TreeSet<String>()
    var leaf = root
    val current = StringBuilder()
    for (ch in prefix) {
      leaf = leaf.childNodes[ch] ?: return emptyList()
      current.append(ch)
    }
    suggestHelper(leaf, resultNames, current)
    val result = ArrayList<AutocompleteItem>(resultNames.size)
    resultNames.forEach { name -> items[name]?.let { result.add(it) } }
    return result
  }
}
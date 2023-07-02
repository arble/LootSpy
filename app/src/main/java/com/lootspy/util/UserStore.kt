package com.lootspy.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserStore(private val context: Context) {
  companion object {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("lootspy")
    private val TOKEN_KEY = stringPreferencesKey("access_token")
    private val MEMBERSHIP_ID = stringPreferencesKey("membership_id")
  }

  val tokenFlow: Flow<String> = context.dataStore.data.map { it[TOKEN_KEY] ?: "" }
  val membershipFlow: Flow<String> = context.dataStore.data.map { it[MEMBERSHIP_ID] ?: "" }

  suspend fun saveAuthInfo(token: String, membershipId: String) {
    context.dataStore.edit {
      it[TOKEN_KEY] = token
      it[MEMBERSHIP_ID] = membershipId
    }
  }
}
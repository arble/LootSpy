package com.lootspy.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Singleton

@Singleton
class UserStore(private val context: Context) {
  companion object {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("lootspy")
    private val TOKEN_KEY = stringPreferencesKey("access_token")
    private val MEMBERSHIP_ID = stringPreferencesKey("membership_id")
    private val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
  }

  val accessToken: Flow<String> = context.dataStore.data.map { it[TOKEN_KEY] ?: "" }
  val membershipId: Flow<String> = context.dataStore.data.map { it[MEMBERSHIP_ID] ?: "" }
  val lastSyncTime: Flow<Long> = context.dataStore.data.map { it[LAST_SYNC_TIME] ?: 0 }

  suspend fun saveAuthInfo(token: String, membershipId: String) {
    context.dataStore.edit {
      it[TOKEN_KEY] = token
      it[MEMBERSHIP_ID] = membershipId
    }
  }

  suspend fun deleteAuthInfo() {
    context.dataStore.edit {
      it.remove(TOKEN_KEY)
      it.remove(MEMBERSHIP_ID)
    }
  }

  suspend fun saveLastSyncTime(time: Long) {
    context.dataStore.edit { it[LAST_SYNC_TIME] = time }
  }
}
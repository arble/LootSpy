package com.lootspy.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.openid.appauth.AuthState
import javax.inject.Singleton

@Singleton
class UserStore(private val context: Context) {
  companion object {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("lootspy")
    private val MEMBERSHIP_ID = stringPreferencesKey("membership_id")
    private val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
    private val PRIMARY_MEMBERSHIP_ID = longPreferencesKey("primary_membership_id")
    private val ACTIVE_MEMBERSHIP_ID = longPreferencesKey("selected_membership_id")
    private val LAST_MANIFEST = stringPreferencesKey("last_manifest")
    private val LAST_MANIFEST_DB = stringPreferencesKey("last_manifest_db")
    private val AUTH_STATE = stringPreferencesKey("auth_state")
    private val ACTIVE_CHARACTER = longPreferencesKey("active_character")
    private val ALWAYS_PATTERNS = booleanPreferencesKey("always_patterns")
  }

  val bungieMembershipId: Flow<String> = context.dataStore.data.map { it[MEMBERSHIP_ID] ?: "" }
  val lastSyncTime: Flow<Long> = context.dataStore.data.map { it[LAST_SYNC_TIME] ?: 0 }
  val primaryMembership: Flow<Long> = context.dataStore.data.map { it[PRIMARY_MEMBERSHIP_ID] ?: 0 }
  val activeMembership: Flow<Long> = context.dataStore.data.map { it[ACTIVE_MEMBERSHIP_ID] ?: 0 }
  val lastManifest: Flow<String> = context.dataStore.data.map { it[LAST_MANIFEST] ?: "" }
  val lastManifestDb: Flow<String> = context.dataStore.data.map { it[LAST_MANIFEST_DB] ?: "" }
  val activeCharacter: Flow<Long> = context.dataStore.data.map { it[ACTIVE_CHARACTER] ?: 0 }
  val alwaysPatterns: Flow<Boolean> = context.dataStore.data.map { it[ALWAYS_PATTERNS] ?: false }
  val authState: Flow<AuthState> = context.dataStore.data.map {
    val authStateString = it[AUTH_STATE]
    return@map if (authStateString != null) {
      AuthState.jsonDeserialize(authStateString)
    } else {
      AuthState()
    }
  }

  suspend fun saveAuthState(authState: AuthState) {
    context.dataStore.edit { it[AUTH_STATE] = authState.jsonSerializeString() }
  }

  suspend fun saveBungieMembership(membershipId: String) {
    context.dataStore.edit { it[MEMBERSHIP_ID] = membershipId }
  }

  suspend fun deleteAuthInfo() {
    context.dataStore.edit {
      it.remove(AUTH_STATE)
      it.remove(MEMBERSHIP_ID)
    }
  }

  suspend fun saveLastSyncTime(time: Long) {
    context.dataStore.edit { it[LAST_SYNC_TIME] = time }
  }

  suspend fun savePrimaryMembership(membershipId: Long) {
    context.dataStore.edit { it[PRIMARY_MEMBERSHIP_ID] = membershipId }
  }

  suspend fun saveActiveMembership(membershipId: Long) {
    context.dataStore.edit { it[ACTIVE_MEMBERSHIP_ID] = membershipId }
  }

  suspend fun saveActiveCharacter(characterId: Long) {
    context.dataStore.edit { it[ACTIVE_CHARACTER] = characterId }
  }

  suspend fun saveAlwaysPatterns(patterns: Boolean) {
    context.dataStore.edit { it[ALWAYS_PATTERNS] = patterns }
  }

  suspend fun saveLastManifest(manifest: String) {
    context.dataStore.edit { it[LAST_MANIFEST] = manifest }
  }

  suspend fun saveLastManifestDb(manifestDb: String) {
    context.dataStore.edit { it[LAST_MANIFEST_DB] = manifestDb }
  }
}
package com.lootspy.di

import android.content.Context
import com.lootspy.data.repo.CharacterRepository
import com.lootspy.data.repo.DefaultCharacterRepository
import com.lootspy.data.repo.DefaultFilterRepository
import com.lootspy.data.repo.DefaultLootRepository
import com.lootspy.data.repo.DefaultProfileRepository
import com.lootspy.data.repo.FilterRepository
import com.lootspy.data.repo.LootRepository
import com.lootspy.data.repo.ProfileRepository
import com.lootspy.data.UserStore
import com.lootspy.data.source.CharacterDao
import com.lootspy.data.source.FilterDao
import com.lootspy.data.source.LootEntryDao
import com.lootspy.data.source.LootSpyDatabase
import com.lootspy.data.source.ProfileDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
  @Singleton
  @Binds
  abstract fun bindLootRepository(repository: DefaultLootRepository): LootRepository

  @Singleton
  @Binds
  abstract fun bindFilterRepository(repository: DefaultFilterRepository): FilterRepository

  @Singleton
  @Binds
  abstract fun bindProfileRepository(repository: DefaultProfileRepository): ProfileRepository

  @Singleton
  @Binds
  abstract fun bindCharacterRepository(repository: DefaultCharacterRepository): CharacterRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
  @Singleton
  @Provides
  fun provideLootSpyDatabase(@ApplicationContext context: Context) =
    LootSpyDatabase.getInstance(context)

  @Singleton
  @Provides
  fun provideUserStore(@ApplicationContext context: Context) = UserStore(context)

  @Provides
  fun provideLootDao(database: LootSpyDatabase): LootEntryDao = database.lootEntryDao()

  @Provides
  fun provideFilterDao(database: LootSpyDatabase): FilterDao = database.filterDao()

  @Provides
  fun provideProfileDao(database: LootSpyDatabase): ProfileDao = database.profileDao()

  @Provides
  fun provideCharacterDao(database: LootSpyDatabase): CharacterDao = database.characterDao()
}
package com.lootspy.di

import android.content.Context
import com.lootspy.data.DefaultFilterRepository
import com.lootspy.data.DefaultLootRepository
import com.lootspy.data.DefaultProfileRepository
import com.lootspy.data.FilterRepository
import com.lootspy.data.LootRepository
import com.lootspy.data.ProfileRepository
import com.lootspy.data.UserStore
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
  fun provideLootDao(database: com.lootspy.data.source.LootSpyDatabase): LootEntryDao =
    database.lootEntryDao()

  @Provides
  fun provideFilterDao(database: com.lootspy.data.source.LootSpyDatabase): FilterDao =
    database.filterDao()

  @Provides
  fun provideProfileDao(database: com.lootspy.data.source.LootSpyDatabase): ProfileDao =
    database.profileDao()
}
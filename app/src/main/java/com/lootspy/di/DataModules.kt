package com.lootspy.di

import android.content.Context
import androidx.room.Room
import com.lootspy.data.DefaultFilterRepository
import com.lootspy.data.DefaultLootRepository
import com.lootspy.data.FilterRepository
import com.lootspy.data.LootRepository
import com.lootspy.data.source.FilterDao
import com.lootspy.data.source.LootEntryDao
import com.lootspy.data.source.LootSpyDatabase
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
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
  @Singleton
  @Provides
  fun provideLootSpyDatabase(@ApplicationContext context: Context): LootSpyDatabase {
    return Room.databaseBuilder(
      context.applicationContext,
      LootSpyDatabase::class.java,
      "matched_loot.db"
    ).fallbackToDestructiveMigration()
      .build()
  }

  @Provides
  fun provideLootDao(database: LootSpyDatabase): LootEntryDao = database.lootEntryDao()

  @Provides
  fun provideFilterDao(database: LootSpyDatabase): FilterDao = database.filterDao()
}
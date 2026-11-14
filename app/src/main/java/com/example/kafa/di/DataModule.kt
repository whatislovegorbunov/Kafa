package com.example.kafa.di

import android.content.Context
import androidx.room.Room
import com.example.kafa.data.local.EventDao
import com.example.kafa.data.local.EventDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DataModule {

    @Provides
    @Singleton
    fun provideEventDatabase(context: Context): EventDatabase =
        Room.databaseBuilder(context, EventDatabase::class.java, "notes.db")
            .addMigrations(EventDatabase.MIGRATION_2_3, EventDatabase.MIGRATION_3_4)
            .build()

    @Provides
    @Singleton
    fun provideEventDao(database: EventDatabase): EventDao = database.eventDao()
}

package com.example.kafa.di

import com.example.kafa.data.api.EventRepository
import com.example.kafa.data.impl.EventRepositoryImpl
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository
}

package com.example.kafa.di

import android.content.Context
import com.example.kafa.MainActivity
import com.example.kafa.data.local.EventDatabase
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, DataModule::class, ViewModelModule::class])
interface AppComponent {
    fun inject(activity: MainActivity)
    fun viewModelFactory(): ViewModelFactory
    fun eventDatabase(): EventDatabase

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }
}

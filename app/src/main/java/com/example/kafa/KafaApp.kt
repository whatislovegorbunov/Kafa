package com.example.kafa

import android.app.Application
import com.example.kafa.di.AppComponent
import com.example.kafa.di.DaggerAppComponent
import com.example.kafa.notifications.ReminderWorker

class KafaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ReminderWorker.schedule(this)
    }

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create(this)
    }
}

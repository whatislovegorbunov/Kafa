package com.example.kafa.di

import androidx.lifecycle.ViewModel
import com.example.kafa.presentation.feature.addevent.AddEventViewModel
import com.example.kafa.presentation.feature.events.EventsViewModel
import com.example.kafa.presentation.feature.reminders.RemindersViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(EventsViewModel::class)
    abstract fun bindEventsViewModel(viewModel: EventsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RemindersViewModel::class)
    abstract fun bindRemindersViewModel(viewModel: RemindersViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddEventViewModel::class)
    abstract fun bindAddEventViewModel(viewModel: AddEventViewModel): ViewModel
}

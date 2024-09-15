package com.example.contactsapp

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module{

    single{
        Room.databaseBuilder(
            androidContext(),
            ContactDatabase::class.java,"contactDatabase"
        ).build()
    }

    single{get<ContactDatabase>().contactDao()}

    single{ContactRepository(get())}

    viewModel{TheViewModel(get())}

}
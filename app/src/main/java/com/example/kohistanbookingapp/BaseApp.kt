package com.example.kohistanbookingapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.kohistanbookingapp.repository.CreateBookingRepository
import com.example.kohistanbookingapp.util.SharedPreferencesHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BaseApp : Application(), Configuration.Provider {
    
    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    
    override fun onCreate() {
        super.onCreate()
        

    }
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

}

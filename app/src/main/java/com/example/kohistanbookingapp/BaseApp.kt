package com.example.kohistanbookingapp

import android.app.Application
import com.example.kohistanbookingapp.repository.ApiRepository
import com.example.kohistanbookingapp.util.SharedPreferencesHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BaseApp : Application() {
    
    @Inject
    lateinit var appStateRepository: ApiRepository
    
    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    
    override fun onCreate() {
        super.onCreate()
        

    }

}

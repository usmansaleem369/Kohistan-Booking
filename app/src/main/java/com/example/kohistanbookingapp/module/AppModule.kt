package com.example.kohistanbookingapp.module

import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.WorkManager
import com.example.kohistanbookingapp.util.PREFERENCE_NAME
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ConnectedNetworkConstraints

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    }
    
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder().serializeNulls().create()
    }

    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager = WorkManager.getInstance(context)

    @Provides
    @Singleton
    @ConnectedNetworkConstraints
    fun provideConnectedNetworkConstraints(): Constraints =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    /*@Provides
    @Singleton
    fun provideGson(): Gson = Gson()*/
}

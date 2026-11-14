package com.example.kohistanbookingapp.module

import android.util.Log
import com.example.kohistanbookingapp.util.SharedPreferencesHelper
import com.example.kohistanbookingapp.api.ApiService
import com.example.kohistanbookingapp.util.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    fun provideOkHttpClient(sharedPreferencesHelper: SharedPreferencesHelper, tokenAuthenticator: TokenAuthenticator): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("OkHttp", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .authenticator(tokenAuthenticator)
            .addInterceptor(loggingInterceptor) // Placed after so logged requests include X-Client-Type and Authorization
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()
                    // Added to every request (including login)
                    .header("X-Client-Type", "mobile")
                    .header("X-Device-Token","mobile")

                // Skip token attachment for login endpoint
                val path = originalRequest.url.encodedPath
                val excludedEndpoints = listOf("login"/*, "logout"*/)

                if (excludedEndpoints.none { path.contains(it, ignoreCase = true) }) {
                    val token = sharedPreferencesHelper.token
                    if (!token.isNullOrBlank()) {
                        requestBuilder.header("Authorization", "Bearer $token")
                    }
                }

                chain.proceed(requestBuilder.build())
            }.build()
    }
    
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    fun provideBaseUrl(sharedPreferencesHelper: SharedPreferencesHelper): String {
        return "http://192.168.55.24:8078/"
    }
    
    @Provides
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
    
   
}

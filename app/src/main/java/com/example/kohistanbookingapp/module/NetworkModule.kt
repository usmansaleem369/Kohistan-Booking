package com.example.kohistanbookingapp.module

import android.util.Log
import com.example.kohistanbookingapp.BuildConfig
import com.example.kohistanbookingapp.api.ApiService
import com.example.kohistanbookingapp.api.PaymentApiService
import com.example.kohistanbookingapp.util.SharedPreferencesHelper
import com.example.kohistanbookingapp.util.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthInterceptor

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LoggingInterceptor

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PaymentOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PaymentRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun provideBaseUrl(): String =
        "http://125.209.84.238:8083/"

    @Provides
    @Singleton
    @AuthInterceptor
    fun provideAuthInterceptor(
        sharedPreferencesHelper: SharedPreferencesHelper
    ): Interceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
            .header("X-Client-Type", "mobile")
            .header("X-Device-Token", "mobile")

        val path = originalRequest.url.encodedPath
        val excludedEndpoints = listOf("login")

        if (excludedEndpoints.none { path.contains(it, ignoreCase = true) }) {
            sharedPreferencesHelper.token
                ?.takeIf { it.isNotBlank() }
                ?.let { token ->
                    requestBuilder.header("Authorization", "Bearer $token")
                }
        }

        chain.proceed(requestBuilder.build())
    }

    @Provides
    @Singleton
    @LoggingInterceptor
    fun provideLoggingInterceptor(): Interceptor =
        HttpLoggingInterceptor { message ->
            Log.d("OkHttp", message)
        }.apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    // Standard client for GET routes, login, seat maps, booking history
    @Provides
    @Singleton
    fun provideOkHttpClient(
        @AuthInterceptor authInterceptor: Interceptor,
        @LoggingInterceptor loggingInterceptor: Interceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .connectionPool(ConnectionPool(5, 30, TimeUnit.SECONDS))
        .retryOnConnectionFailure(true)
        .authenticator(tokenAuthenticator)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    // Dedicated payment client: zero automatic socket retries + 90s read timeout for MPIN approval
    @Provides
    @Singleton
    @PaymentOkHttpClient
    fun providePaymentOkHttpClient(
        okHttpClient: OkHttpClient,
    ): OkHttpClient = okHttpClient.newBuilder()
        .retryOnConnectionFailure(false)
        .readTimeout(90, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        baseUrl: String
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @PaymentRetrofit
    fun providePaymentRetrofit(
        @PaymentOkHttpClient paymentOkHttpClient: OkHttpClient,
        baseUrl: String
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(paymentOkHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun providePaymentApiService(
        @PaymentRetrofit paymentRetrofit: Retrofit
    ): PaymentApiService =
        paymentRetrofit.create(PaymentApiService::class.java)
}
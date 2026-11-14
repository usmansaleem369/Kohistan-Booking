package com.example.kohistanbookingapp.util

import android.util.Log
import com.example.kohistanbookingapp.api.ApiService
import com.example.kohistanbookingapp.models.request.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val apiServiceProvider: Provider<ApiService>
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath
        if (path.contains("RefreshToken", ignoreCase = true) ||
            path.contains("Login", ignoreCase = true)
        ) {
            return null
        }

        if (responseCount(response) >= 2) {
            return null
        }

        val currentRefreshToken = sharedPreferencesHelper.refreshToken
        if (currentRefreshToken.isBlank()) {
            return null
        }

        synchronized(this) {
            val currentToken = sharedPreferencesHelper.token
            val requestToken = response.request.header("Authorization")?.replace("Bearer ", "")

            if (currentToken != requestToken && currentToken?.isNotBlank() ?: false) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            return runBlocking {
                try {
                    val apiService = apiServiceProvider.get()
                    val refreshResponse = apiService.refreshToken(
                        RefreshTokenRequest(refreshToken = currentRefreshToken)
                    )

                    if (refreshResponse.isSuccessful && refreshResponse.body()?.data != null) {
                        val newTokens = refreshResponse.body()!!.data!!
                        Log.d("TokenAuthenticator", "Token refreshed successfully")

                        sharedPreferencesHelper.token = newTokens.accessToken
                        sharedPreferencesHelper.refreshToken = newTokens.refreshToken

                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${newTokens.accessToken}")
                            .build()
                    } else {
                        sharedPreferencesHelper.clearUserData()
                        null
                    }
                } catch (e: Exception) {
                    Log.e("TokenAuthenticator", "Error refreshing token: ${e.message}")
                    null
                }
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
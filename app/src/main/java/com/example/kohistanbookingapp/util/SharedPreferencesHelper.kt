package com.example.kohistanbookingapp.util

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import javax.inject.Inject

class SharedPreferencesHelper @Inject constructor(private val sharedPreferences: SharedPreferences) {
    companion object {
        private const val TOKEN = "accessToken"
        private const val REFRESH_TOKEN = "refreshToken"
        private const val CLIENT_ID = "clientId"
        private const val CLIENT_SECRET = "clientSecret"
        private const val NOTIFICATION_TOKEN = "notificationToken"

        private const val EMAIL_KEY = "USER_EMAIL"
        private const val USERNAME_KEY = "USER_USERNAME"

        private const val PASSWORD_KEY = "USER_PASSWORD"
        private const val DISPLAY_NAME = "DISPLAY_NAME"
    }

    private val gson = Gson()


    var token: String?
        get() = sharedPreferences.getString(TOKEN, null)
        set(value) = sharedPreferences.edit { putString(TOKEN, value) }

    var refreshToken: String?
        get() = sharedPreferences.getString(REFRESH_TOKEN, "")
        set(value) = sharedPreferences.edit { putString(REFRESH_TOKEN, value) }

    var clientId: String?
        get() = sharedPreferences.getString(CLIENT_ID, "")
        set(value) = sharedPreferences.edit { putString(CLIENT_ID, value) }

    var clientSecret: String?
        get() = sharedPreferences.getString(CLIENT_SECRET, "")
        set(value) = sharedPreferences.edit { putString(CLIENT_SECRET, value) }

    var notificationToken: String?
        get() = sharedPreferences.getString(NOTIFICATION_TOKEN, "")
        set(value) = sharedPreferences.edit { putString(NOTIFICATION_TOKEN, value) }

    var email: String?
        get() = sharedPreferences.getString(EMAIL_KEY, null)
        set(value) {
            if (value == null) sharedPreferences.edit { remove(EMAIL_KEY) }
            else sharedPreferences.edit { putString(EMAIL_KEY, value) }
        }

    var username: String?
        get() = sharedPreferences.getString(USERNAME_KEY, null)
        set(value) {
            if (value == null) sharedPreferences.edit { remove(USERNAME_KEY) }
            else sharedPreferences.edit { putString(USERNAME_KEY, value) }
        }

    var displayName: String?
        get() = sharedPreferences.getString(DISPLAY_NAME, null)
        set(value) {
            if (value == null) sharedPreferences.edit { remove(DISPLAY_NAME) }
            else sharedPreferences.edit { putString(DISPLAY_NAME, value) }
        }


}

package com.example.kohistanbookingapp.util

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.kohistanbookingapp.models.response.LoginResponse
import com.google.gson.Gson
import javax.inject.Inject

class SharedPreferencesHelper @Inject constructor(private val sharedPreferences: SharedPreferences) {
    companion object {
        private const val TOKEN = "accessToken"
        private const val REFRESH_TOKEN = "refreshToken"
        private const val TOKEN_EXPIRES_AT = "accessTokenExpiresAt"
        private const val CLIENT_ID = "clientId"
        private const val CLIENT_SECRET = "clientSecret"
        private const val NOTIFICATION_TOKEN = "notificationToken"

        private const val EMAIL_KEY = "USER_EMAIL"
        private const val USERNAME_KEY = "USER_USERNAME"
        private const val PASSWORD_KEY = "USER_PASSWORD"
        private const val DISPLAY_NAME = "DISPLAY_NAME"

        private const val MEMBER_ID = "MEMBER_ID"
        private const val MEMBERSHIP_CARD_NO = "MEMBERSHIP_CARD_NO"
        private const val USER_DATA_JSON = "USER_DATA_JSON"
    }

    private val gson = Gson()

    var token: String?
        get() = sharedPreferences.getString(TOKEN, null)
        set(value) = sharedPreferences.edit { putString(TOKEN, value) }

    var refreshToken: String?
        get() = sharedPreferences.getString(REFRESH_TOKEN, "")
        set(value) = sharedPreferences.edit { putString(REFRESH_TOKEN, value) }

    var accessTokenExpiresAt: String?
        get() = sharedPreferences.getString(TOKEN_EXPIRES_AT, null)
        set(value) {
            if (value == null) sharedPreferences.edit { remove(TOKEN_EXPIRES_AT) }
            else sharedPreferences.edit { putString(TOKEN_EXPIRES_AT, value) }
        }

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

    var memberId: Long
        get() = sharedPreferences.getLong(MEMBER_ID, -1L)
        set(value) = sharedPreferences.edit { putLong(MEMBER_ID, value) }

    var membershipCardNo: String?
        get() = sharedPreferences.getString(MEMBERSHIP_CARD_NO, null)
        set(value) {
            if (value == null) sharedPreferences.edit { remove(MEMBERSHIP_CARD_NO) }
            else sharedPreferences.edit { putString(MEMBERSHIP_CARD_NO, value) }
        }

    fun saveUserSession(loginData: LoginResponse?) {
        if (loginData == null) return

        token = loginData.accessToken
        refreshToken = loginData.refreshToken
        accessTokenExpiresAt = loginData.accessTokenExpiresAt
        memberId = loginData.memberId ?: -1L
        membershipCardNo = loginData.membershipCardNo
        username = loginData.userName
        displayName = loginData.fullName

        sharedPreferences.edit {
            putString(USER_DATA_JSON, gson.toJson(loginData))
        }
    }

    fun getUserData(): LoginResponse? {
        val json = sharedPreferences.getString(USER_DATA_JSON, null) ?: return null
        return try {
            gson.fromJson(json, LoginResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }

    val isLoggedIn: Boolean
        get() = !token.isNullOrBlank()

    fun clearUserData() {
        sharedPreferences.edit {
            remove(TOKEN)
            remove(REFRESH_TOKEN)
            remove(TOKEN_EXPIRES_AT)
            remove(MEMBER_ID)
            remove(MEMBERSHIP_CARD_NO)
            remove(USERNAME_KEY)
            remove(DISPLAY_NAME)
            remove(EMAIL_KEY)
            remove(USER_DATA_JSON)
        }
    }
}
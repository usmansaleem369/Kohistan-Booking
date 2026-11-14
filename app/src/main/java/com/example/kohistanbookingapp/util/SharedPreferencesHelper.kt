package com.example.kohistanbookingapp.util

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.kohistanbookingapp.models.response.LoginResponse
import com.example.kohistanbookingapp.models.response.ProfileData
import com.example.kohistanbookingapp.models.response.RouteData
import com.example.kohistanbookingapp.models.response.SelectedSeat
import com.google.gson.Gson
import javax.inject.Inject

class SharedPreferencesHelper @Inject constructor(private val sharedPreferences: SharedPreferences) {
    companion object {
        private const val TOKEN = "accessToken"
        private const val REFRESH_TOKEN = "refreshToken"
        private const val TOKEN_EXPIRES_AT = "accessTokenExpiresAt"

        private const val EMAIL_KEY = "USER_EMAIL"
        private const val USERNAME_KEY = "USER_USERNAME"
        private const val USER_DATA_JSON = "USER_DATA_JSON"
        private const val SELECTED_TRAVEL_DATE = "SELECTED_TRAVEL_DATE"
        private const val SELECTED_ROUTE_DATA = "SELECTED_ROUTE_DATA"

        private const val KEY_SELECTED_SEATS = "selected_seats_list"

        private const val KEY_USER_PROFILE = "user_profile_data"
    }

    private val gson = Gson()

    var token: String?
        get() = sharedPreferences.getString(TOKEN, null)
        set(value) = sharedPreferences.edit { putString(TOKEN, value) }

    var refreshToken: String
        get() = sharedPreferences.getString(REFRESH_TOKEN, "")?:""
        set(value) = sharedPreferences.edit { putString(REFRESH_TOKEN, value) }

    var accessTokenExpiresAt: String?
        get() = sharedPreferences.getString(TOKEN_EXPIRES_AT, null)
        set(value) {
            if (value == null) sharedPreferences.edit { remove(TOKEN_EXPIRES_AT) }
            else sharedPreferences.edit { putString(TOKEN_EXPIRES_AT, value) }
        }


    var email: String?
        get() = sharedPreferences.getString(EMAIL_KEY, null)
        set(value) {
            if (value == null) sharedPreferences.edit { remove(EMAIL_KEY) }
            else sharedPreferences.edit { putString(EMAIL_KEY, value) }
        }



    fun saveUserSession(loginData: LoginResponse?) {
        if (loginData == null) return

        token = loginData.accessToken
        refreshToken = loginData.refreshToken
        accessTokenExpiresAt = loginData.accessTokenExpiresAt

        sharedPreferences.edit {
            putString(USER_DATA_JSON, gson.toJson(loginData))
        }
    }

    fun getUserSession(): LoginResponse? {
        val json = sharedPreferences.getString(USER_DATA_JSON, null) ?: return null
        return try {
            gson.fromJson(json, LoginResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }

    val isLoggedIn: Boolean
        get() = !token.isNullOrBlank()

    var selectedTravelDate: String?
        get() = sharedPreferences.getString(SELECTED_TRAVEL_DATE, null)
        set(value) {
            sharedPreferences.edit {
                if (value.isNullOrBlank()) {
                    remove(SELECTED_TRAVEL_DATE)
                } else {
                    putString(SELECTED_TRAVEL_DATE, value)
                }
            }
        }

    var selectedRouteData: RouteData?
        get() {
            val json = sharedPreferences.getString(SELECTED_ROUTE_DATA, null) ?: return null
            return try {
                gson.fromJson(json, RouteData::class.java)
            } catch (e: Exception) {
                null
            }
        }
        set(value) {
            sharedPreferences.edit {
                if (value == null) {
                    remove(SELECTED_ROUTE_DATA)
                } else {
                    putString(SELECTED_ROUTE_DATA, gson.toJson(value))
                }
            }
        }

    var selectedSeats: List<SelectedSeat>
        get() {
            val json = sharedPreferences.getString(KEY_SELECTED_SEATS, null) ?: return emptyList()
            return try {
                val type = object : com.google.gson.reflect.TypeToken<List<SelectedSeat>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
        set(value) {
            if (value.isEmpty()) {
                sharedPreferences.edit { remove(KEY_SELECTED_SEATS) }
            } else {
                sharedPreferences.edit { putString(KEY_SELECTED_SEATS, gson.toJson(value)) }
            }
        }

    var userProfile: ProfileData?
        get() {
            val json = sharedPreferences.getString(KEY_USER_PROFILE, null) ?: return null
            return try {
                gson.fromJson(json, ProfileData::class.java)
            } catch (e: Exception) {
                null
            }
        }
        set(value) {
            if (value == null) {
                sharedPreferences.edit { remove(KEY_USER_PROFILE) }
            } else {
                sharedPreferences.edit { putString(KEY_USER_PROFILE, gson.toJson(value)) }
            }
        }

    fun clearUserData() {
        sharedPreferences.edit {
            remove(TOKEN)
            remove(REFRESH_TOKEN)
            remove(TOKEN_EXPIRES_AT)
            remove(EMAIL_KEY)
            remove(USER_DATA_JSON)
            remove(KEY_SELECTED_SEATS)
            remove(SELECTED_TRAVEL_DATE)
            remove(SELECTED_ROUTE_DATA)
        }
    }

    fun clearTicketSession(){
        sharedPreferences.edit {
            remove(KEY_SELECTED_SEATS)
            remove(SELECTED_TRAVEL_DATE)
            remove(SELECTED_ROUTE_DATA)
        }
    }
}
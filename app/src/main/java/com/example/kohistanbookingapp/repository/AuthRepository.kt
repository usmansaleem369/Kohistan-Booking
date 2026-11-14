package com.example.kohistanbookingapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.kohistanbookingapp.api.ApiRequestStatus
import com.example.kohistanbookingapp.api.ApiService
import com.example.kohistanbookingapp.models.request.CreateProfileRequest
import com.example.kohistanbookingapp.models.request.UpdateProfileRequest
import com.example.kohistanbookingapp.models.response.ApiResponse
import com.example.kohistanbookingapp.models.response.LoginResponse
import com.example.kohistanbookingapp.models.response.ProfileData
import com.example.kohistanbookingapp.util.Event
import com.example.kohistanbookingapp.util.ExceptionHandler
import com.example.kohistanbookingapp.util.SharedPreferencesHelper
import com.example.kohistanbookingapp.util.handleResponse
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val sharedPreferences: SharedPreferencesHelper
) {
    private var isRequestInProgress = false

    private val _apiRequestStatus = MutableLiveData<Event<ApiRequestStatus>>()
    val apiRequestStatus: LiveData<Event<ApiRequestStatus>> = _apiRequestStatus


    private val _loginResponse = MutableLiveData<Event<LoginResponse>>()
    val loginResponse: LiveData<Event<LoginResponse>> = _loginResponse

    private val _logoutResponse = MutableLiveData<Event<ApiResponse<Unit?>>>()
    val logoutResponse: LiveData<Event<ApiResponse<Unit?>>> = _logoutResponse

    private val _createProfileResponse = MutableLiveData<Event<ProfileData>>()
    val createProfileResponse: LiveData<Event<ProfileData>> = _createProfileResponse

    private val _updateProfileResponse = MutableLiveData<Event<ProfileData>>()
    val updateProfileResponse: LiveData<Event<ProfileData>> = _updateProfileResponse
    suspend fun login(username: String, password: String) {
        isRequestInProgress = true
        _apiRequestStatus.postValue(Event(ApiRequestStatus.Loading))

        try {
            val response = apiService.loginUser(username, password)
            _apiRequestStatus.postValue(Event(ApiRequestStatus.Idle))
            response.handleResponse(
                onSuccess = { res ->
                    res.data?.let { response ->
                        sharedPreferences.token = response.accessToken
                        sharedPreferences.saveUserSession(response)
                        _loginResponse.postValue(Event(response))
                    } ?: run {
                        _apiRequestStatus.postValue(Event(ApiRequestStatus.Error("No cities found.")))
                    }
                },
                onError = { error ->
                    _apiRequestStatus.postValue(Event(ApiRequestStatus.Error(error)))
                }
            )
        } catch (e: Exception) {
            _apiRequestStatus.postValue(Event(ApiRequestStatus.Error(ExceptionHandler.getMessage(e))))
        } finally {
            isRequestInProgress = false
        }
    }

    suspend fun logout() {
        isRequestInProgress = true
        _apiRequestStatus.postValue(Event(ApiRequestStatus.Loading))

        try {
            val response = apiService.logout()
            _apiRequestStatus.postValue(Event(ApiRequestStatus.Idle))
            response.handleResponse(
                onSuccess = { res ->
                    sharedPreferences.clearUserData()
                    _logoutResponse.postValue(Event(res))
                },
                onError = { error ->
                    _apiRequestStatus.postValue(Event(ApiRequestStatus.Error(error)))
                }
            )
        } catch (e: Exception) {
            _apiRequestStatus.postValue(Event(ApiRequestStatus.Error(ExceptionHandler.getMessage(e))))
        } finally {
            isRequestInProgress = false
        }
    }

    suspend fun createProfile(createProfileRequest: CreateProfileRequest) {
        isRequestInProgress = true
        _apiRequestStatus.postValue(Event(ApiRequestStatus.Loading))

        try {
            val response = apiService.createProfile(createProfileRequest)
            _apiRequestStatus.postValue(Event(ApiRequestStatus.Idle))
            response.handleResponse(
                onSuccess = { res ->
                    res.data?.let {profileData ->
                        sharedPreferences.userProfile = profileData
                        _createProfileResponse.postValue(Event(profileData))
                    }
                },
                onError = { error ->
                    _apiRequestStatus.postValue(Event(ApiRequestStatus.Error(error)))
                }
            )
        } catch (e: Exception) {
            _apiRequestStatus.postValue(Event(ApiRequestStatus.Error(ExceptionHandler.getMessage(e))))
        } finally {
            isRequestInProgress = false
        }
    }

    suspend fun updateProfile(updateProfileRequest: UpdateProfileRequest) {
        isRequestInProgress = true
        _apiRequestStatus.postValue(Event(ApiRequestStatus.Loading))

        try {
            val response = apiService.updateProfile(updateProfileRequest)
            _apiRequestStatus.postValue(Event(ApiRequestStatus.Idle))
            response.handleResponse(
                onSuccess = { res ->
                    res.data?.let {profileData ->
                        sharedPreferences.userProfile = profileData
                        _updateProfileResponse.postValue(Event(profileData))
                    }
                },
                onError = { error ->
                    _apiRequestStatus.postValue(Event(ApiRequestStatus.Error(error)))
                }
            )
        } catch (e: Exception) {
            _apiRequestStatus.postValue(Event(ApiRequestStatus.Error(ExceptionHandler.getMessage(e))))
        } finally {
            isRequestInProgress = false
        }
    }

}
package com.example.kohistanbookingapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kohistanbookingapp.models.request.CreateProfileRequest
import com.example.kohistanbookingapp.models.request.UpdateProfileRequest
import com.example.kohistanbookingapp.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel

class AuthViewModel @Inject constructor(
    application: Application,
    private val authRepository: AuthRepository
) : AndroidViewModel(application)  {

    val apiRequestStatus = authRepository.apiRequestStatus
    val loginResponse = authRepository.loginResponse

    val logoutResponse = authRepository.logoutResponse

    val createProfileResponse = authRepository.createProfileResponse
    val updateProfileResponse = authRepository.updateProfileResponse

    val getProfileResponse = authRepository.getProfileResponse
    fun login(username: String,password: String){
        viewModelScope.launch {
            authRepository.login(username,password)
        }
    }

    fun logout(){
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun createProfile(createProfileRequest: CreateProfileRequest){
        viewModelScope.launch {
            authRepository.createProfile(createProfileRequest)
        }
    }

    fun updateProfile(updateProfileRequest: UpdateProfileRequest){
        viewModelScope.launch {
            authRepository.updateProfile(updateProfileRequest)
        }
    }

    fun getProfile(memberId:Int){
        viewModelScope.launch {
            authRepository.getProfile(memberId)
        }
    }

}
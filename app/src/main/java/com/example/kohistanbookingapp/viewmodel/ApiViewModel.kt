package com.example.kohistanbookingapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kohistanbookingapp.models.response.CityData
import com.example.kohistanbookingapp.repository.ApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApiViewModel @Inject constructor(
    application: Application,
    private val apiRepository: ApiRepository
) : AndroidViewModel(application) {

    val apiRequestStatus = apiRepository.apiRequestStatus
    val citiesData = apiRepository.citiesData

    val allRoutesData = apiRepository.allRoutesData

    val selectedFromCity = apiRepository.selectedFromCity
    val selectedToCity = apiRepository.selectedToCity

    val filteredRoutes = apiRepository.filteredRoutes

    val selectedUtcDate = apiRepository.selectedUtcDate

    fun setFromCity(city: CityData?) = apiRepository.setSelectedFromCity(city)
    fun setToCity(city: CityData?) = apiRepository.setSelectedToCity(city)
    fun swapCities() = apiRepository.swapCities()

    fun setSelectedDate(formattedUtcDate: String) {
        apiRepository.setSelectedDate(formattedUtcDate)
    }

    fun getSelectedDate(): String?{
        return apiRepository.getSelectedDate()
    }

    fun getAllCities(){
        viewModelScope.launch {
            apiRepository.getAllCities()
        }
    }

    fun getAllRoutes(){
        viewModelScope.launch {
            apiRepository.getAllRoutes()
        }
    }

    fun searchRoutes(){
        apiRepository.searchRoutes()
    }
}

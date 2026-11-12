package com.example.kohistanbookingapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.kohistanbookingapp.models.request.CreateBookingRequest
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
    val allDepartureCities = apiRepository.allDepartureCities

    val allDestinationCities = apiRepository.allDestinationCities


    val selectedFromCity = apiRepository.selectedFromCity
    val selectedToCity = apiRepository.selectedToCity

    val routeDetailResponse = apiRepository.routeDetailResponse

    val seatPatternResponse = apiRepository.seatPatternResponse

    val bookedSeatsResponse = apiRepository.bookedSeatsResponse
    val createBookingResponse = apiRepository.createBookingResponse
    fun setFromCity(city: CityData?) = apiRepository.setSelectedFromCity(city)
    fun setToCity(city: CityData?) = apiRepository.setSelectedToCity(city)
    fun swapCities() = apiRepository.swapCities()

    fun getAllDepartureCities(){
        viewModelScope.launch {
            apiRepository.getAllDepartureCities()
        }
    }

    fun getAllDestinationCities(){
        viewModelScope.launch {
            apiRepository.getAllDestinationCities()
        }
    }

    fun getRouteDetails(date: String){
        viewModelScope.launch {
            apiRepository.getRouteDetails(date)
        }
    }

    fun getSeatPattern(vehicleTypeId:Int){
        viewModelScope.launch {
            apiRepository.getSeatPattern(vehicleTypeId)
        }
    }

    fun getBookedSeats() {
        viewModelScope.launch {
            apiRepository.getBookedSeats()
        }
    }

    fun createBooking(request: CreateBookingRequest) {
        viewModelScope.launch {
            apiRepository.createBooking(request)
        }
    }

    fun clearSession(){
        apiRepository.clearSession()
    }
}

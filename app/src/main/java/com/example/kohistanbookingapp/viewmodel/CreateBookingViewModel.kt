package com.example.kohistanbookingapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kohistanbookingapp.models.request.CreateBookingRequest
import com.example.kohistanbookingapp.repository.CreateBookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateBookingViewModel @Inject constructor(
    application: Application,
    private val createBookingRepository: CreateBookingRepository
) : AndroidViewModel(application) {

    val apiRequestStatus = createBookingRepository.apiRequestStatus
    val allDepartureCities = createBookingRepository.allDepartureCities
    val allDestinationCities = createBookingRepository.allDestinationCities

    val routeDetailResponse = createBookingRepository.routeDetailResponse
    val seatPatternResponse = createBookingRepository.seatPatternResponse
    val bookedSeatsResponse = createBookingRepository.bookedSeatsResponse
    val createBookingResponse = createBookingRepository.createBookingResponse

    fun getAllDepartureCities() {
        viewModelScope.launch {
            createBookingRepository.getAllDepartureCities()
        }
    }

    fun getAllDestinationCities() {
        viewModelScope.launch {
            createBookingRepository.getAllDestinationCities()
        }
    }

    fun getRouteDetails(date: String) {
        viewModelScope.launch {
            createBookingRepository.getRouteDetails(date)
        }
    }

    fun getSeatPattern(vehicleTypeId: Int) {
        viewModelScope.launch {
            createBookingRepository.getSeatPattern(vehicleTypeId)
        }
    }

    fun getBookedSeats() {
        viewModelScope.launch {
            createBookingRepository.getBookedSeats()
        }
    }

    fun createBooking(request: CreateBookingRequest) {
        viewModelScope.launch {
            createBookingRepository.createOnlineBookingWithWorker(request)
        }
    }

    fun clearSession() {
        createBookingRepository.clearSession()
    }
}
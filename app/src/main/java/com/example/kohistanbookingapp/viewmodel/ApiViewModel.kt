package com.example.kohistanbookingapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.kohistanbookingapp.models.request.CreateBookingRequest
import com.example.kohistanbookingapp.models.response.BookingHistoryItem
import com.example.kohistanbookingapp.models.response.BookingHistoryResponse
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
    val bookingHistoryResponse = apiRepository.bookingHistoryResponse

    // Pagination state preserved across configuration changes
    private val _bookingsList = MutableLiveData<List<BookingHistoryItem>>(emptyList())
    val bookingsList: LiveData<List<BookingHistoryItem>> = _bookingsList

    private val _totalRecords = MutableLiveData(0)
    val totalRecords: LiveData<Int> = _totalRecords

    private val _isLoadingMore = MutableLiveData(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private var currentPage = 0
    private var totalPages = 1
    private val pageSize = 10

    fun setFromCity(city: CityData?) = apiRepository.setSelectedFromCity(city)
    fun setToCity(city: CityData?) = apiRepository.setSelectedToCity(city)
    fun swapCities() = apiRepository.swapCities()

    fun getAllDepartureCities() {
        viewModelScope.launch {
            apiRepository.getAllDepartureCities()
        }
    }

    fun getAllDestinationCities() {
        viewModelScope.launch {
            apiRepository.getAllDestinationCities()
        }
    }

    fun getRouteDetails(date: String) {
        viewModelScope.launch {
            apiRepository.getRouteDetails(date)
        }
    }

    fun getSeatPattern(vehicleTypeId: Int) {
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

    fun loadInitialBookingsIfNeeded(memberId: Int) {
        if (currentPage == 0) {
            getBookingHistory(memberId = memberId, pageNumber = 1, isRefresh = true)
        }
    }

    fun loadNextBookingsPage(memberId: Int) {
        if (_isLoadingMore.value == true || currentPage >= totalPages) return
        _isLoadingMore.value = true
        getBookingHistory(memberId = memberId, pageNumber = currentPage + 1, isRefresh = false)
    }

    fun getBookingHistory(memberId: Int, pageNumber: Int = 1, isRefresh: Boolean = pageNumber == 1) {
        if (!isRefresh) {
            _isLoadingMore.value = true
        }
        viewModelScope.launch {
            apiRepository.getBookingHistory(
                memberId = memberId,
                pageNumber = pageNumber,
                pageSize = pageSize
            )
        }
    }

    fun onBookingHistoryFetched(response: BookingHistoryResponse) {
        _isLoadingMore.value = false

        val meta = response.meta
        currentPage = meta?.pageNumber ?: 1
        totalPages = meta?.totalPages ?: 1

        val currentItems = if (currentPage == 1) emptyList() else _bookingsList.value.orEmpty()
        val merged = (currentItems + response.data).distinctBy { it.transactionId }

        _bookingsList.value = merged
        _totalRecords.value = meta?.totalRecords ?: merged.size
    }

    fun onBookingHistoryError() {
        _isLoadingMore.value = false
    }

    fun clearSession() {
        currentPage = 0
        totalPages = 1
        _bookingsList.value = emptyList()
        _totalRecords.value = 0
        _isLoadingMore.value = false
        apiRepository.clearSession()
    }
}
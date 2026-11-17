package com.example.kohistanbookingapp.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.kohistanbookingapp.api.ApiRequestStatus
import com.example.kohistanbookingapp.api.ApiService
import com.example.kohistanbookingapp.models.request.CreateBookingRequest
import com.example.kohistanbookingapp.models.response.BookedSeatsData
import com.example.kohistanbookingapp.models.response.CityData
import com.example.kohistanbookingapp.models.response.CreateBookingData
import com.example.kohistanbookingapp.models.response.RouteData
import com.example.kohistanbookingapp.models.response.SeatPatternRow
import com.example.kohistanbookingapp.util.Event
import com.example.kohistanbookingapp.util.ExceptionHandler
import com.example.kohistanbookingapp.util.SharedPreferencesHelper
import com.example.kohistanbookingapp.util.handleResponse
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateBookingRepository @Inject constructor(
    private val apiService: ApiService,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val sharedRepository: SharedRepository
) {
    private var isRequestInProgress = false

    private val _apiRequestStatus = MutableLiveData<Event<ApiRequestStatus>>()
    val apiRequestStatus: LiveData<Event<ApiRequestStatus>> = _apiRequestStatus

    // ==================== Route & City Data ====================
    private val _allDepartureCities = MutableLiveData<Event<List<CityData>?>>()
    val allDepartureCities: LiveData<Event<List<CityData>?>> = _allDepartureCities

    private val _allDestinationCities = MutableLiveData<Event<List<CityData>?>>()
    val allDestinationCities: LiveData<Event<List<CityData>?>> = _allDestinationCities

    private val _routeDetailResponse = MutableLiveData<Event<List<RouteData>>>()
    val routeDetailResponse: LiveData<Event<List<RouteData>>> = _routeDetailResponse

    private val _seatPatternResponse = MutableLiveData<Event<List<SeatPatternRow>>>()
    val seatPatternResponse: LiveData<Event<List<SeatPatternRow>>> = _seatPatternResponse

    private val _bookedSeatsResponse = MutableLiveData<Event<BookedSeatsData>>()
    val bookedSeatsResponse: LiveData<Event<BookedSeatsData>> = _bookedSeatsResponse

    private val _createBookingResponse = MutableLiveData<Event<CreateBookingData>>()
    val createBookingResponse: LiveData<Event<CreateBookingData>> = _createBookingResponse

    // ==================== API Calls ====================
    suspend fun getAllDepartureCities() {
        isRequestInProgress = true
        _apiRequestStatus.postValue(Event(ApiRequestStatus.Loading))

        try {
            val response = apiService.getAllDepartureCities()
            _apiRequestStatus.postValue(Event(ApiRequestStatus.Idle))
            response.handleResponse(
                onSuccess = { res ->
                    res.data?.let { cities ->
                        _allDepartureCities.postValue(Event(cities))
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

    suspend fun getAllDestinationCities() {
        isRequestInProgress = true
        _apiRequestStatus.postValue(Event(ApiRequestStatus.Loading))

        try {
            val response = apiService.getAllDestinationCities()
            _apiRequestStatus.postValue(Event(ApiRequestStatus.Idle))
            response.handleResponse(
                onSuccess = { res ->
                    res.data?.let { cities ->
                        _allDestinationCities.postValue(Event(cities))
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
    fun formatToIsoDate(encodedDate: String): String {
        return try {
            val decoded = URLDecoder.decode(encodedDate, "UTF-8") // "Wed, 11 Nov 2026"
            val inputFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val date = inputFormat.parse(decoded)
            date?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            ""
        }
    }
    suspend fun getRouteDetails(date: String) {
        val fromCityId = sharedRepository.selectedFromCity.value?.cityId
        val toCityId = sharedRepository.selectedToCity.value?.cityId
        val date = formatToIsoDate(date)


        Log.e("Cities",sharedRepository.selectedFromCity.value.toString())
        if (fromCityId == null || toCityId == null || date.isNullOrBlank()) {
            _apiRequestStatus.postValue(
                Event(ApiRequestStatus.Error("Please select departure city, destination city, and date."))
            )
            return
        }

        isRequestInProgress = true
        _apiRequestStatus.postValue(Event(ApiRequestStatus.Loading))

        try {
            val response = apiService.getRouteDetails(
                fromCityId = fromCityId,
                toCityId = toCityId,
                date = date
            )
            _apiRequestStatus.postValue(Event(ApiRequestStatus.Idle))
            response.handleResponse(
                onSuccess = { res ->
                    res.data?.let { details ->
                        _routeDetailResponse.postValue(Event(details))
                    } ?: run {
                        _routeDetailResponse.postValue(Event(emptyList()))
                        _apiRequestStatus.postValue(Event(ApiRequestStatus.Error("No route time details found.")))
                    }
                },
                onError = { error ->
                    _routeDetailResponse.postValue(Event(emptyList()))
                    _apiRequestStatus.postValue(Event(ApiRequestStatus.Error(error)))
                }
            )
        } catch (e: Exception) {
            _apiRequestStatus.postValue(Event(ApiRequestStatus.Error(ExceptionHandler.getMessage(e))))
        } finally {
            isRequestInProgress = false
        }
    }

    suspend fun getSeatPattern(vehicleTypeId: Int) {
        isRequestInProgress = true
        _apiRequestStatus.postValue(Event(ApiRequestStatus.Loading))

        try {
            val response = apiService.getSeatPattern(vehicleTypeId = vehicleTypeId)
            _apiRequestStatus.postValue(Event(ApiRequestStatus.Idle))
            response.handleResponse(
                onSuccess = { res ->
                    res.data?.let { pattern ->
                        _seatPatternResponse.postValue(Event(pattern))
                    } ?: run {
                        _seatPatternResponse.postValue(Event(emptyList()))
                        _apiRequestStatus.postValue(Event(ApiRequestStatus.Error("No seat pattern found.")))
                    }
                },
                onError = { error ->
                    _seatPatternResponse.postValue(Event(emptyList()))
                    _apiRequestStatus.postValue(Event(ApiRequestStatus.Error(error)))
                }
            )
        } catch (e: Exception) {
            _apiRequestStatus.postValue(Event(ApiRequestStatus.Error(ExceptionHandler.getMessage(e))))
        } finally {
            isRequestInProgress = false
        }
    }

    suspend fun getBookedSeats() {
        val departureDate = sharedPreferencesHelper.selectedTravelDate?:""
        val isoDate = if (departureDate.contains(",")) formatToIsoDate(departureDate) else departureDate
        val routeDetails = sharedPreferencesHelper.selectedRouteData
        isRequestInProgress = true
        _apiRequestStatus.postValue(Event(ApiRequestStatus.Loading))

        try {
            val response = apiService.getBookedSeats(
                routeId = routeDetails?.routeId?:-1,
                routeTimeId = routeDetails?.routeTimeId?:-1,
                departureDate = isoDate,
                fromCityId = routeDetails?.fromCityId?:-1,
                toCityId = routeDetails?.toCityId?:-1
            )
            _apiRequestStatus.postValue(Event(ApiRequestStatus.Idle))
            response.handleResponse(
                onSuccess = { res ->
                    res.data?.let { data ->
                        _bookedSeatsResponse.postValue(Event(data))
                    } ?: run {
                        _apiRequestStatus.postValue(Event(ApiRequestStatus.Error("No booked seat details found.")))
                    }
                },
                onError = { error ->
                    if (!error.contains("No seats are booked", ignoreCase = true)) {
                        _apiRequestStatus.postValue(Event(ApiRequestStatus.Error(error)))
                    } else {
                        _bookedSeatsResponse.postValue(Event(BookedSeatsData(seats = emptyList())))
                    }
                }
            )
        } catch (e: Exception) {
            _apiRequestStatus.postValue(Event(ApiRequestStatus.Error(ExceptionHandler.getMessage(e))))
        } finally {
            isRequestInProgress = false
        }
    }

    suspend fun createBooking(request: CreateBookingRequest) {
        val formattedRequest = if (request.departureDate.contains(",")) {
            request.copy(departureDate = formatToIsoDate(request.departureDate))
        } else {
            request
        }

        isRequestInProgress = true
        _apiRequestStatus.postValue(Event(ApiRequestStatus.Loading))

        try {
            val response = apiService.createBooking(formattedRequest)
            _apiRequestStatus.postValue(Event(ApiRequestStatus.Idle))
            response.handleResponse(
                onSuccess = { res ->
                    res.data?.let { bookingData ->
                        _createBookingResponse.postValue(Event(bookingData))
                    } ?: run {
                        _apiRequestStatus.postValue(Event(ApiRequestStatus.Error("Reservation details missing in response.")))
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

    fun clearSession() {
        _allDestinationCities.postValue(Event(null))
        _allDepartureCities.postValue(Event(null))
    }
}
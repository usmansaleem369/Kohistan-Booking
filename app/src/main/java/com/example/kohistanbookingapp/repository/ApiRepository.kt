package com.example.kohistanbookingapp.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.kohistanbookingapp.api.ApiRequestStatus
import com.example.kohistanbookingapp.api.ApiService
import com.example.kohistanbookingapp.models.request.CreateBookingRequest
import com.example.kohistanbookingapp.models.response.ApiResponse
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
class ApiRepository @Inject constructor(
    private val apiService: ApiService,
    private val sharedPreferencesHelper: SharedPreferencesHelper
) {
    private var isRequestInProgress = false

    private val _apiRequestStatus = MutableLiveData<Event<ApiRequestStatus>>()
    val apiRequestStatus: LiveData<Event<ApiRequestStatus>> = _apiRequestStatus

    // ==================== Selection State (LiveData) ====================
    private val _selectedFromCity = MutableLiveData<CityData?>()
    val selectedFromCity: LiveData<CityData?> = _selectedFromCity

    private val _selectedToCity = MutableLiveData<CityData?>()
    val selectedToCity: LiveData<CityData?> = _selectedToCity


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

    // ==================== Getters & Setters ====================

    fun setSelectedFromCity(city: CityData?) {
        _selectedFromCity.postValue(city)
    }

    fun setSelectedToCity(city: CityData?) {
        _selectedToCity.postValue(city)
    }

    fun getSelectedFromCity(): CityData? = _selectedFromCity.value

    fun getSelectedToCity(): CityData? = _selectedToCity.value

    fun swapCities() {
        val temp = _selectedFromCity.value
        _selectedFromCity.value = _selectedToCity.value
        _selectedToCity.value = temp
    }

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
        val fromCityId = _selectedFromCity.value?.cityId
        val toCityId = _selectedToCity.value?.cityId
        val date = formatToIsoDate(date)


        Log.e("Cities",_selectedFromCity.value.toString())
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
        _selectedFromCity.postValue(null)
        _selectedToCity.postValue(null)
        _allDestinationCities.postValue(null)
        _allDepartureCities.postValue(null)
    }
}

/*// ==================== Route Search Logic ====================
   fun searchRoutes() {
       val fromCityCode = _selectedFromCity.value?.cityCode?.trim()
       val toCityCode = _selectedToCity.value?.cityCode?.trim()

       Log.e("Route", "From : $fromCityCode")
       Log.e("Route", "To: $toCityCode")

       if (fromCityCode.isNullOrEmpty() || toCityCode.isNullOrEmpty()) {
           _filteredRoutes.postValue(Event(emptyList()))
           return
       }

       val routes = _routeDetailResponse.value?.lastContent().orEmpty()

       val result = routes.filter { route ->
           val dropPoints = route.dropPointCollection.orEmpty()

           val toCityIndex = dropPoints.indexOfFirst {
               it.dropPointName?.trim()?.contains(toCityCode, ignoreCase = true) == true
           }

           if (toCityIndex == -1) {
               return@filter false
           }

           val originIsSameAsFrom = route.routeCode?.split("-")?.firstOrNull()
               ?.contains(fromCityCode, ignoreCase = true) == true

           val fromIndex = dropPoints.indexOfFirst {
               it.dropPointName?.trim().equals(fromCityCode, ignoreCase = true)
           }

           when {
               fromIndex != -1 -> fromIndex < toCityIndex
               originIsSameAsFrom -> true
               else -> false
           }
       }

       _filteredRoutes.postValue(Event(result))
   }*/
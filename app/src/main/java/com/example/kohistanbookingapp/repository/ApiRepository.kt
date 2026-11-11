package com.example.kohistanbookingapp.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.kohistanbookingapp.api.ApiRequestStatus
import com.example.kohistanbookingapp.api.ApiService
import com.example.kohistanbookingapp.models.response.CityData
import com.example.kohistanbookingapp.models.response.RouteData
import com.example.kohistanbookingapp.util.Event
import com.example.kohistanbookingapp.util.ExceptionHandler
import com.example.kohistanbookingapp.util.handleResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiRepository @Inject constructor(
    private val apiService: ApiService
) {
    private var isRequestInProgress = false

    private val _apiRequestStatus = MutableLiveData<Event<ApiRequestStatus>>()
    val apiRequestStatus: LiveData<Event<ApiRequestStatus>> = _apiRequestStatus

    // ==================== Selection State (LiveData) ====================
    private val _selectedFromCity = MutableLiveData<CityData?>()
    val selectedFromCity: LiveData<CityData?> = _selectedFromCity

    private val _selectedToCity = MutableLiveData<CityData?>()
    val selectedToCity: LiveData<CityData?> = _selectedToCity

    private val _selectedUtcDate = MutableLiveData<String?>()
    val selectedUtcDate: LiveData<String?> = _selectedUtcDate

    // ==================== Route & City Data ====================
    private val _filteredRoutes = MutableLiveData< Event<List<RouteData>>>()
    val filteredRoutes: LiveData<Event<List<RouteData>>> = _filteredRoutes

    private val _citiesData = MutableLiveData<Event<List<CityData>>>()
    val citiesData: LiveData<Event<List<CityData>>> = _citiesData

    private val _allRoutesData = MutableLiveData<Event<List<RouteData>>>()
    val allRoutesData: LiveData<Event<List<RouteData>>> = _allRoutesData

    // ==================== Getters & Setters ====================
    fun setSelectedDate(formattedUtcDate: String?) {
        _selectedUtcDate.postValue(formattedUtcDate)
    }

    fun getSelectedDate(): String? = _selectedUtcDate.value

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
    suspend fun getAllCities() {
        isRequestInProgress = true
        _apiRequestStatus.postValue(Event(ApiRequestStatus.Loading))

        try {
            val response = apiService.getAllCities()
            _apiRequestStatus.postValue(Event(ApiRequestStatus.Idle))
            response.handleResponse(
                onSuccess = { res ->
                    res.data?.let { cities ->
                        _citiesData.postValue(Event(cities))
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

    suspend fun getAllRoutes() {
        isRequestInProgress = true
        _apiRequestStatus.postValue(Event(ApiRequestStatus.Loading))

        try {
            val response = apiService.getAllRoutes()
            _apiRequestStatus.postValue(Event(ApiRequestStatus.Idle))
            response.handleResponse(
                onSuccess = { res ->
                    res.data?.let { routes ->
                        _allRoutesData.postValue(Event(routes))
                    } ?: run {
                        _apiRequestStatus.postValue(Event(ApiRequestStatus.Error("No routes found.")))
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

    // ==================== Route Search Logic ====================
    fun searchRoutes() {
        val fromCityCode = _selectedFromCity.value?.cityCode?.trim()
        val toCityCode = _selectedToCity.value?.cityCode?.trim()

        Log.e("Route", "From : $fromCityCode")
        Log.e("Route", "To: $toCityCode")

        if (fromCityCode.isNullOrEmpty() || toCityCode.isNullOrEmpty()) {
            _filteredRoutes.postValue(Event(emptyList()))
            return
        }

        val routes = _allRoutesData.value?.lastContent().orEmpty()

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
    }

    fun clearSession() {
        _selectedUtcDate.postValue(null)
        _selectedFromCity.postValue(null)
        _selectedToCity.postValue(null)
    }
}
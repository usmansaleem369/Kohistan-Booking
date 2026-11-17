package com.example.kohistanbookingapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.kohistanbookingapp.models.response.CityData
import com.example.kohistanbookingapp.util.Event
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedRepository @Inject constructor() {

    private val _selectedFromCity = MutableLiveData<CityData?>()
    val selectedFromCity: LiveData<CityData?> = _selectedFromCity

    private val _selectedToCity = MutableLiveData<CityData?>()
    val selectedToCity: LiveData<CityData?> = _selectedToCity

    fun setSelectedFromCity(city: CityData?) {
        _selectedFromCity.postValue(city)
    }

    fun setSelectedToCity(city: CityData?) {
        _selectedToCity.postValue(city)
    }

    fun swapCities() {
        val temp = _selectedFromCity.value
        _selectedFromCity.value = _selectedToCity.value
        _selectedToCity.value = temp
    }


    fun clearSession() {
        _selectedFromCity.postValue(null)
        _selectedToCity.postValue(null)
    }
}
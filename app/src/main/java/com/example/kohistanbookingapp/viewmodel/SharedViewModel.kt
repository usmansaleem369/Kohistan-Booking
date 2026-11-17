package com.example.kohistanbookingapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.kohistanbookingapp.models.response.CityData
import com.example.kohistanbookingapp.repository.SharedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    application: Application,
    private val sharedRepository: SharedRepository
) : AndroidViewModel(application)  {

    val selectedFromCity = sharedRepository.selectedFromCity
    val selectedToCity = sharedRepository.selectedToCity

    fun setFromCity(city: CityData?) = sharedRepository.setSelectedFromCity(city)
    fun setToCity(city: CityData?) = sharedRepository.setSelectedToCity(city)
    fun swapCities() = sharedRepository.swapCities()
}
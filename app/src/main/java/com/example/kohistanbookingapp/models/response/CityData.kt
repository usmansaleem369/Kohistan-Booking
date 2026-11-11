package com.example.kohistanbookingapp.models.response

import com.google.gson.annotations.SerializedName

data class CityData(
    @SerializedName("CityId")
    val cityId: Int? = null,

    @SerializedName("CityName")
    val cityName: String? = null,

    @SerializedName("CityCode")
    val cityCode: String? = null
)

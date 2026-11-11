package com.example.kohistanbookingapp.models.response

import com.google.gson.annotations.SerializedName

data class RouteData(
    @SerializedName("RouteId")
    val routeId: Int? = null,

    @SerializedName("DropPointId")
    val dropPointId: Int? = null,

    @SerializedName("VehicleTypeId")
    val vehicleTypeId: Int? = null,

    @SerializedName("RouteTimeId")
    val routeTimeId: Long? = null,

    @SerializedName("FromCity")
    val fromCity: String? = null,

    @SerializedName("ToCity")
    val toCity: String? = null,

    @SerializedName("RouteTimeValue")
    val routeTimeValue: String? = null,

    @SerializedName("RouteTimeType")
    val routeTimeType: String? = null,

    @SerializedName("Fare")
    val fare: Double? = null,

    @SerializedName("VehicleType")
    val vehicleType: String? = null,

    @SerializedName("SeatCapacity")
    val seatCapacity: Int? = null,

    @SerializedName("DisableSeats")
    val disableSeats: String? = null,

    @SerializedName("RemainingSeats")
    val remainingSeats: Int? = null
)

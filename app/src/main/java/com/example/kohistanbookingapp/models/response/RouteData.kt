package com.example.kohistanbookingapp.models.response

import com.google.gson.annotations.SerializedName

data class RouteData(
    @SerializedName("IsChecked")
    val isChecked: Boolean? = null,

    @SerializedName("TerminalId")
    val terminalId: Int? = null,

    @SerializedName("RouteId")
    val routeId: Int? = null,

    @SerializedName("RouteFrom")
    val routeFrom: String? = null,

    @SerializedName("RouteTo")
    val routeTo: String? = null,

    @SerializedName("RouteCode")
    val routeCode: String? = null,

    @SerializedName("VehicleTypeId")
    val vehicleTypeId: Int? = null,

    @SerializedName("RouteTimeCollection")
    val routeTimeCollection: List<RouteTime>? = null,

    @SerializedName("DropPointCollection")
    val dropPointCollection: List<DropPoint>? = null
)

data class RouteTime(
    @SerializedName("RouteTimeId")
    val routeTimeId: Long? = null,

    @SerializedName("RouteTime")
    val routeTime: String? = null,

    @SerializedName("TimeType")
    val timeType: String? = null,

    @SerializedName("DisableSeats")
    val disableSeats: String? = null,

    @SerializedName("ExtraFare")
    val extraFare: Double? = null,

    @SerializedName("TotalSeatsToDisplay")
    val totalSeatsToDisplay: Int? = null,

    @SerializedName("SchecduledDate")
    val scheduledDate: String? = null,

    @SerializedName("VehicleTypeId")
    val vehicleTypeId: Int? = null,

    @SerializedName("IsShowOnWeb")
    val isShowOnWeb: Boolean? = null,

)

data class DropPoint(
    @SerializedName("DropPointId")
    val dropPointId: Int? = null,

    @SerializedName("DropPointName")
    val dropPointName: String? = null,

    @SerializedName("Fare")
    val fare: Double? = null,

    @SerializedName("Mileage")
    val mileage: Int? = null
)

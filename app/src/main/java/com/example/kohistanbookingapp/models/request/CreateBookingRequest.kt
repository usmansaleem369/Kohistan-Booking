package com.example.kohistanbookingapp.models.request

import com.google.gson.annotations.SerializedName

data class CreateBookingRequest(
    @SerializedName("TerminalId")
    val terminalId: Int,

    @SerializedName("RouteId")
    val routeId: Int,

    @SerializedName("DropPointId")
    val dropPointId: Int,

    @SerializedName("VehicleTypeId")
    val vehicleTypeId: Int,

    @SerializedName("RouteTimeId")
    val routeTimeId: Long,

    @SerializedName("DepartureDate")
    val departureDate: String, // "yyyy-MM-dd"

    @SerializedName("SeatNumbers")
    val seatNumbers: List<Int>,

    @SerializedName("Title")
    val title: String, // "Mr", "Mrs", "Miss"

    @SerializedName("PassengerName")
    val passengerName: String,

    @SerializedName("Cnic")
    val cnic: String,

    @SerializedName("ContactNo")
    val contactNo: String,

    @SerializedName("Email")
    val email: String? = null
)
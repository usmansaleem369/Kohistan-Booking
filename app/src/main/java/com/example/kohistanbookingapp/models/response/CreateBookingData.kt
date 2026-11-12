package com.example.kohistanbookingapp.models.response

import com.google.gson.annotations.SerializedName

data class CreateBookingData(
    @SerializedName("TransactionId")
    val transactionId: Long? = null,

    @SerializedName("ReservationId")
    val reservationId: String? = null,

    @SerializedName("RouteName")
    val routeName: String? = null,

    @SerializedName("DropPoint")
    val dropPoint: String? = null,

    @SerializedName("DepartureDate")
    val departureDate: String? = null,

    @SerializedName("DepartureTime")
    val departureTime: String? = null,

    @SerializedName("TimeType")
    val timeType: String? = null,

    @SerializedName("PassengerName")
    val passengerName: String? = null,

    @SerializedName("ContactNo")
    val contactNo: String? = null,

    @SerializedName("TotalFare")
    val totalFare: Double? = null,

    @SerializedName("Seats")
    val seats: List<ReservedSeatItem>? = null
)

data class ReservedSeatItem(
    @SerializedName("SeatNo")
    val seatNo: Int? = null,

    @SerializedName("Fare")
    val fare: Double? = null,

    @SerializedName("Gender")
    val gender: String? = null,

    @SerializedName("TicketType")
    val ticketType: String? = null
)
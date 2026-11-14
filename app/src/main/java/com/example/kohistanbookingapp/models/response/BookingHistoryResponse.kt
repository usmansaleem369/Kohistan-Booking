package com.example.kohistanbookingapp.models.response

import com.google.gson.annotations.SerializedName

data class BookingHistoryResponse(
    @SerializedName("Code")
    val code: Int,

    @SerializedName("Message")
    val message: String,

    @SerializedName("Data")
    val data: List<BookingHistoryItem> = emptyList(),

    @SerializedName("Meta")
    val meta: PaginationMeta? = null
)

data class BookingHistoryItem(
    @SerializedName("TransactionId")
    val transactionId: Long,

    @SerializedName("ReservationId")
    val reservationId: String,

    @SerializedName("BookedAt")
    val bookedAt: String,

    @SerializedName("RouteId")
    val routeId: Int,

    @SerializedName("RouteName")
    val routeName: String,

    @SerializedName("FromCityId")
    val fromCityId: Int,

    @SerializedName("FromCity")
    val fromCity: String,

    @SerializedName("ToCityId")
    val toCityId: Int,

    @SerializedName("ToCity")
    val toCity: String,

    @SerializedName("DropPointId")
    val dropPointId: Int,

    @SerializedName("DropPoint")
    val dropPoint: String,

    @SerializedName("RouteTimeId")
    val routeTimeId: Int,

    @SerializedName("DepartureTime")
    val departureTime: String,

    @SerializedName("DepartureDate")
    val departureDate: String,

    @SerializedName("TimeType")
    val timeType: String,

    @SerializedName("PassengerName")
    val passengerName: String,

    @SerializedName("Cnic")
    val cnic: String,

    @SerializedName("ContactNo")
    val contactNo: String,

    @SerializedName("TotalFare")
    val totalFare: Double,

    @SerializedName("Seats")
    val seats: List<BookedSeatItem> = emptyList()
)


data class PaginationMeta(
    @SerializedName("TotalRecords")
    val totalRecords: Int,

    @SerializedName("PageNumber")
    val pageNumber: Int,

    @SerializedName("PageSize")
    val pageSize: Int,

    @SerializedName("TotalPages")
    val totalPages: Int
)
package com.example.kohistanbookingapp.models.response

import com.google.gson.annotations.SerializedName

data class BookedSeatsData(
    @SerializedName("RouteId") val routeId: Int? = null,
    @SerializedName("RouteCode") val routeCode: String? = null,
    @SerializedName("FromCityId") val fromCityId: Int? = null,
    @SerializedName("FromCity") val fromCity: String? = null,
    @SerializedName("ToCityId") val toCityId: Int? = null,
    @SerializedName("ToCity") val toCity: String? = null,
    @SerializedName("RouteTimeId") val routeTimeId: Long? = null,
    @SerializedName("RouteTimeValue") val routeTimeValue: String? = null,
    @SerializedName("DepartureDate") val departureDate: String? = null,
    @SerializedName("Seats") val seats: List<BookedSeatItem>? = null
) {
    fun getMaleBookedSeatSet(): Set<Int> {
        return seats.orEmpty()
            .filter { it.gender.equals("male", ignoreCase = true) }
            .mapNotNull { it.seatNo }
            .toSet()
    }

    fun getFemaleBookedSeatSet(): Set<Int> {
        return seats.orEmpty()
            .filter { it.gender.equals("female", ignoreCase = true) }
            .mapNotNull { it.seatNo }
            .toSet()
    }
}

data class BookedSeatItem(
    @SerializedName("SeatNo") val seatNo: Int? = null,
    @SerializedName("Fare") val fare: Double? = null,
    @SerializedName("Gender") val gender: String? = null,
    @SerializedName("TicketType") val ticketType: String? = null
)
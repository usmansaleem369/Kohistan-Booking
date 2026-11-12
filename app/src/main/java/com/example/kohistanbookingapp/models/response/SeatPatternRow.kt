package com.example.kohistanbookingapp.models.response

import com.google.gson.annotations.SerializedName

data class SeatPatternRow(
    @SerializedName("Id") val id: Int? = null,
    @SerializedName("VehicleTypeId") val vehicleTypeId: Int? = null,
    @SerializedName("VehicleId") val vehicleId:Int? = null,
    @SerializedName("C1") val c1: Int = -1,
    @SerializedName("C2") val c2: Int = -1,
    @SerializedName("C3") val c3: Int = -1,
    @SerializedName("C4") val c4: Int = -1,
    @SerializedName("C5") val c5: Int = -1
)

enum class SeatGender {
    NONE,
    MALE,
    FEMALE
}

enum class SeatStatus {
    AISLE,          // -1
    AVAILABLE,      // Free to select
    BOOKED_MALE,    // Pre-booked by a male passenger
    BOOKED_FEMALE,  // Pre-booked by a female passenger
    SELECTED        // Selected in the user's current booking
}

data class SeatCell(
    val seatNumber: Int,
    val rowIndex: Int,
    val colIndex: Int, // 0..4 (C1..C5)
    val status: SeatStatus,
    val gender: SeatGender = SeatGender.NONE
)

data class SelectedSeat(
    val seatNumber: Int,
    val requiredGender: SeatGender? = null,
    var assignedGender: SeatGender = requiredGender ?: SeatGender.MALE
)
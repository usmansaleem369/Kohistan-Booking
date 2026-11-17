package com.example.kohistanbookingapp.models.request

import com.google.gson.annotations.SerializedName

data class CancelBookingRequest(
    @SerializedName("TransactionId") val transactionId: Long,
    @SerializedName("SeatNos") val seatNos: List<Int>
)

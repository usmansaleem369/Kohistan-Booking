package com.example.kohistanbookingapp.models.request

import com.google.gson.annotations.SerializedName

data class JazzCashPayRequest(
    @SerializedName("BookingTransactionId")
    val bookingTransactionId: Long,

    @SerializedName("Origin")
    val origin: String = "Mobile",

    @SerializedName("AccCnic")
    val accCnic: String? = null,

    @SerializedName("AccMobileNumber")
    val accMobileNumber: String,

)
package com.example.kohistanbookingapp.models.request

import com.google.gson.annotations.SerializedName

data class JazzCashPayRequest(
    @SerializedName("TransactionId")
    val transactionId: String,

    @SerializedName("BookingTransactionId")
    val bookingTransactionId: Long,

    @SerializedName("Origin")
    val origin: String = "Mobile",

    @SerializedName("ReceivedAmount")
    val receivedAmount: Double,

    @SerializedName("AccCnic")
    val accCnic: String? = null,

    @SerializedName("AccMobileNumber")
    val accMobileNumber: String,

    @SerializedName("Remarks")
    val remarks: String? = null,

    @SerializedName("Notes")
    val notes: String? = null
)
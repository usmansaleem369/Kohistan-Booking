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
    val seats: List<ReservedSeatItem>? = null,

    @SerializedName("Payment")
    val payment: BookingPaymentInfo? = null,

    @SerializedName("PaymentMessage")
    val paymentMessage: String? = null
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

data class BookingPaymentInfo(
    @SerializedName("Id")
    val id: Long? = null,

    @SerializedName("TransactionId")
    val transactionId: String? = null,

    @SerializedName("BookingTransactionId")
    val bookingTransactionId: Long? = null,

    @SerializedName("Origin")
    val origin: String? = null,

    @SerializedName("ReceivedAmount")
    val receivedAmount: Double? = null,

    @SerializedName("TxnDateTime")
    val txnDateTime: String? = null,

    @SerializedName("TxnStatus")
    val txnStatus: String? = null, // "Success" or "Error"

    @SerializedName("AccCnic")
    val accCnic: String? = null,

    @SerializedName("AccMobileNumber")
    val accMobileNumber: String? = null,

    @SerializedName("ResponseCode")
    val responseCode: String? = null, // e.g., "000" = success, "922" = invalid account/MPIN

    @SerializedName("ResponseMessage")
    val responseMessage: String? = null,

    @SerializedName("RetreivalReferenceNo")
    val retrievalReferenceNo: String? = null,

    @SerializedName("Remarks")
    val remarks: String? = null
) {
    val isPaid: Boolean
        get() = txnStatus.equals("Success", ignoreCase = true) || responseCode == "000"
}
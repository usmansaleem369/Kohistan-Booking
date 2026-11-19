package com.example.kohistanbookingapp.api

import com.example.kohistanbookingapp.models.request.CreateBookingRequest
import com.example.kohistanbookingapp.models.request.JazzCashPayRequest
import com.example.kohistanbookingapp.models.response.ApiResponse
import com.example.kohistanbookingapp.models.response.BookingPaymentInfo
import com.example.kohistanbookingapp.models.response.CreateBookingData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface PaymentApiService {

    @Headers("Connection: close")
    @POST("/api/EPayment/PayBookingOnline")
    suspend fun payAfterBooking(
        @Body request: JazzCashPayRequest
    ): Response<ApiResponse<BookingPaymentInfo>>

    @Headers("Connection: close")
    @POST("/api/Booking/CreateBookingWithPayment")
    suspend fun createOnlineBooking(
        @Body request: CreateBookingRequest
    ): Response<ApiResponse<CreateBookingData>>
}
package com.example.kohistanbookingapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.kohistanbookingapp.api.ApiRequestStatus
import com.example.kohistanbookingapp.api.ApiService
import com.example.kohistanbookingapp.models.request.CancelBookingRequest
import com.example.kohistanbookingapp.models.request.JazzCashPayRequest
import com.example.kohistanbookingapp.models.response.ApiResponse
import com.example.kohistanbookingapp.models.response.BookingHistoryResponse
import com.example.kohistanbookingapp.models.response.BookingPaymentInfo
import com.example.kohistanbookingapp.util.Event
import com.example.kohistanbookingapp.util.ExceptionHandler
import com.example.kohistanbookingapp.util.SharedPreferencesHelper
import com.example.kohistanbookingapp.util.handleResponse
import java.net.SocketTimeoutException
import javax.inject.Inject

class BookingHistoryRepository @Inject constructor(
    private val apiService: ApiService,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
) {

    private var isRequestInProgress = false

    private val _apiRequestStatus = MutableLiveData<Event<ApiRequestStatus>>()
    val apiRequestStatus: LiveData<Event<ApiRequestStatus>> = _apiRequestStatus

    private val _bookingHistoryResponse = MutableLiveData<Event<BookingHistoryResponse>>()
    val bookingHistoryResponse: LiveData<Event<BookingHistoryResponse>> = _bookingHistoryResponse

    private val _cancelBookingResponse = MutableLiveData<Event<ApiResponse<Unit>>>()
    val cancelBookingResponse: LiveData<Event<ApiResponse<Unit>>> = _cancelBookingResponse

    private val _payAfterBookingResponse = MutableLiveData<Event<ApiResponse<BookingPaymentInfo>>>()
    val payAfterBookingResponse: LiveData<Event<ApiResponse<BookingPaymentInfo>>> = _payAfterBookingResponse

    suspend fun getBookingHistory(memberId: Int, pageNumber: Int = 1, pageSize: Int = 10, status: String) {
        isRequestInProgress = true
        _apiRequestStatus.postValue(Event(ApiRequestStatus.Loading))

        try {
            val response = apiService.getBookingHistory(
                memberId = memberId,
                pageNumber = pageNumber,
                pageSize = pageSize,
                status = status
            )
            _apiRequestStatus.postValue(Event(ApiRequestStatus.Idle))
            response.handleResponse(
                onSuccess = { res ->
                    _bookingHistoryResponse.postValue(Event(res))
                },
                onError = { error ->
                    if (error.contains("No bookings found", ignoreCase = true)) {
                        _bookingHistoryResponse.postValue(Event(BookingHistoryResponse(code = 200, message = "")))
                    } else {
                        _apiRequestStatus.postValue(Event(ApiRequestStatus.Error(error)))
                    }
                }
            )
        } catch (e: Exception) {
            _apiRequestStatus.postValue(Event(ApiRequestStatus.Error(ExceptionHandler.getMessage(e))))
        } finally {
            isRequestInProgress = false
        }
    }

    suspend fun cancelBooking(cancelBookingRequest: CancelBookingRequest) {
        _apiRequestStatus.postValue(Event(ApiRequestStatus.Loading))
        try {
            val response = apiService.cancelBooking(cancelBookingRequest)

            response.handleResponse(
                onSuccess = {
                    _apiRequestStatus.postValue(Event(ApiRequestStatus.Idle))
                    _cancelBookingResponse.postValue(Event(it))
                },
                onError = { message ->
                    _apiRequestStatus.postValue(
                        Event(ApiRequestStatus.Error(message))
                    )
                }
            )
        } catch (e: Exception) {
            _apiRequestStatus.postValue(
                Event(ApiRequestStatus.Error(e.localizedMessage ?: "Network error occurred"))
            )
        }
    }

    suspend fun payAfterBooking(request: JazzCashPayRequest) {
        _apiRequestStatus.postValue(Event(ApiRequestStatus.Loading))
        try {
            val response = apiService.payAfterBooking(request)

            response.handleResponse(
                onSuccess = { res ->
                    val paymentInfo = res.data
                    if (paymentInfo == null || paymentInfo.isPaid) {
                        _apiRequestStatus.postValue(Event(ApiRequestStatus.Idle))
                        _payAfterBookingResponse.postValue(Event(res))
                    } else {
                        val failMsg = paymentInfo.responseMessage
                            ?.takeIf { it.isNotBlank() }
                            ?: res.message
                            ?: "JazzCash payment could not be completed."
                        _apiRequestStatus.postValue(Event(ApiRequestStatus.Error(failMsg,showDialog = true)))
                    }
                },
                onError = { errorMessage ->
                    _apiRequestStatus.postValue(
                        Event(ApiRequestStatus.Error(errorMessage,showDialog = true))
                    )
                }
            )
        } catch (e: SocketTimeoutException) {
            _apiRequestStatus.postValue(
                Event(
                    ApiRequestStatus.Error(
                        "Payment request timed out. Please refresh My Bookings before trying again."
                    )
                )
            )
        } catch (e: Exception) {
            _apiRequestStatus.postValue(
                Event(ApiRequestStatus.Error(ExceptionHandler.getMessage(e)))
            )
        }
    }
}
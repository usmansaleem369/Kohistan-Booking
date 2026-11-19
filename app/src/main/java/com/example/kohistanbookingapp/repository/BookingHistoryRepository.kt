package com.example.kohistanbookingapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkInfo
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
import com.example.kohistanbookingapp.worker.JazzCashPaymentWorker
import com.example.kohistanbookingapp.worker.PaymentWorkScheduler
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject

class BookingHistoryRepository @Inject constructor(
    private val apiService: ApiService,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val paymentWorkScheduler: PaymentWorkScheduler,
    private val gson: Gson
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

    suspend fun payAfterBookingWithWorker(request: JazzCashPayRequest) {


        try {
            _apiRequestStatus.postValue(Event(ApiRequestStatus.Loading))

            val finalWorkInfo = paymentWorkScheduler.enqueuePayAfterBooking(request)

            when (finalWorkInfo?.state) {
                WorkInfo.State.SUCCEEDED -> {
                    val responseJson = finalWorkInfo.outputData
                        .getString(JazzCashPaymentWorker.KEY_RESPONSE_JSON)
                    val responseType = object : TypeToken<ApiResponse<BookingPaymentInfo>>() {}.type
                    val parsedResponse: ApiResponse<BookingPaymentInfo>? = responseJson?.let {
                        gson.fromJson(it, responseType)
                    }

                    _apiRequestStatus.postValue(Event(ApiRequestStatus.Idle))
                    if (parsedResponse != null) {
                        _payAfterBookingResponse.postValue(Event(parsedResponse))
                    }
                }

                WorkInfo.State.FAILED,
                WorkInfo.State.CANCELLED -> {
                    val errorMsg = finalWorkInfo.outputData
                        .getString(JazzCashPaymentWorker.KEY_ERROR)
                        ?: "Connection interrupted while confirming payment. Please refresh My Bookings to verify status before paying again."
                    val isJazzCashError = finalWorkInfo.outputData
                        .getBoolean(JazzCashPaymentWorker.KEY_IS_JAZZCASH_ERROR, false) ||
                            errorMsg.contains("jazzcash", ignoreCase = true) ||
                            errorMsg.contains("mpin", ignoreCase = true)
                    _apiRequestStatus.postValue(
                        Event(ApiRequestStatus.Error(errorMsg, showDialog = errorMsg.contains("JazzCash",ignoreCase = isJazzCashError)))
                    )
                }

                else -> {
                    _apiRequestStatus.postValue(Event(ApiRequestStatus.Idle))
                }
            }
        } finally {
        }
    }

}
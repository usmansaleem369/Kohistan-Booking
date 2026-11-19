package com.example.kohistanbookingapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.kohistanbookingapp.R
import com.example.kohistanbookingapp.api.PaymentApiService
import com.example.kohistanbookingapp.models.request.CreateBookingRequest
import com.example.kohistanbookingapp.models.request.JazzCashPayRequest
import com.example.kohistanbookingapp.util.ExceptionHandler
import com.example.kohistanbookingapp.util.JazzCashFailureMessageHandler.formatJazzCashFailureMessage
import com.example.kohistanbookingapp.util.handleResponse
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException

@HiltWorker
class JazzCashPaymentWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val paymentApiService: PaymentApiService
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun getForegroundInfo(): ForegroundInfo {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Processing JazzCash Payment")
            .setContentText("Waiting for MPIN confirmation...")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    override suspend fun doWork(): Result {
        try {
            setForeground(getForegroundInfo())
        } catch (_: Exception) {

        }
        if (runAttemptCount > 0) {
            return Result.failure(
                workDataOf(
                    KEY_ERROR to "Payment request was interrupted. Please refresh My Bookings to verify status before paying again."
                )
            )
        }

        val requestJson = inputData.getString(KEY_REQUEST_JSON)
            ?: return Result.failure(workDataOf(KEY_ERROR to "Invalid payment request data."))

        val workType = inputData.getString(KEY_WORK_TYPE) ?: TYPE_PAY_AFTER_BOOKING
        val gson = Gson()

        return try {
            when (workType) {
                TYPE_CREATE_ONLINE_BOOKING -> handleCreateOnlineBooking(requestJson, gson)
                else -> handlePayAfterBooking(requestJson, gson)
            }
        } catch (e: Exception) {
            val cause = (e as? IOException)?.cause ?: e
            val isInterrupted = e is SocketException || cause is SocketException ||
                    e is SocketTimeoutException || cause is SocketTimeoutException

            val message = if (isInterrupted) {
                ExceptionHandler.getPaymentMessage(e)
            } else {
                ExceptionHandler.getMessage(e)
            }
            Result.failure(workDataOf(KEY_ERROR to message))
        }
    }

    private suspend fun handlePayAfterBooking(
        requestJson: String,
        gson: Gson
    ): Result {
        val request = gson.fromJson(requestJson, JazzCashPayRequest::class.java)
        val response = paymentApiService.payAfterBooking(request)
        var workResult: Result = Result.failure(
            workDataOf(KEY_ERROR to "Unexpected payment response.")
        )

        response.handleResponse(
            onSuccess = { res ->
                val paymentInfo = res.data
                workResult = if (paymentInfo == null || paymentInfo.isPaid) {
                    Result.success(
                        workDataOf(KEY_RESPONSE_JSON to gson.toJson(res))
                    )
                } else {
                    val rawMessage = paymentInfo.responseMessage
                        ?.takeIf { it.isNotBlank() }
                        ?: res.message
                    val failMsg = formatJazzCashFailureMessage(rawMessage)
                    Result.failure(workDataOf(KEY_ERROR to failMsg,KEY_IS_JAZZCASH_ERROR to true))
                }
            },
            onError = { errorMessage ->
                val failMsg = formatJazzCashFailureMessage(errorMessage)
                workResult = Result.failure(workDataOf(KEY_ERROR to failMsg,KEY_IS_JAZZCASH_ERROR to isJazzCashGatewayError(failMsg)))
            }
        )

        return workResult
    }

    private suspend fun handleCreateOnlineBooking(requestJson: String, gson: Gson): Result {
        val request = gson.fromJson(requestJson, CreateBookingRequest::class.java)
        val response = paymentApiService.createOnlineBooking(request)
        var workResult: Result = Result.failure(
            workDataOf(KEY_ERROR to "Unexpected booking response.")
        )

        response.handleResponse(
            onSuccess = { res ->
                val bookingData = res.data
                val paymentInfo = bookingData?.payment

                workResult = if (bookingData != null && (paymentInfo == null || paymentInfo.isPaid)) {
                    Result.success(
                        workDataOf(KEY_RESPONSE_JSON to gson.toJson(res))
                    )
                } else {
                    val rawMessage = paymentInfo?.responseMessage?.takeIf { it.isNotBlank() }
                        ?: bookingData?.paymentMessage?.takeIf { it.isNotBlank() }
                        ?: res.message?.takeIf { it.isNotBlank() }
                    val failMsg = formatJazzCashFailureMessage(rawMessage)
                    val isJazzCashError = paymentInfo != null || isJazzCashGatewayError(rawMessage) || isJazzCashGatewayError(failMsg)
                    Result.failure(workDataOf(KEY_ERROR to failMsg, KEY_IS_JAZZCASH_ERROR to isJazzCashError)
                    )
                }
            },
            onError = { errorMessage ->
                val failMsg = formatJazzCashFailureMessage(errorMessage)
                val isJazzCashError = isJazzCashGatewayError(errorMessage) || isJazzCashGatewayError(failMsg)
                workResult = Result.failure(workDataOf(KEY_ERROR to failMsg, KEY_IS_JAZZCASH_ERROR to isJazzCashError))
            }
        )

        return workResult
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Payment Processing",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun isJazzCashGatewayError(rawMessage: String?): Boolean {
        if (rawMessage.isNullOrBlank()) return false
        val keywords = listOf("jazzcash", "mpin", "short message 'n'", "cancel a transaction", "insufficient", "mobile account")
        return keywords.any { rawMessage.contains(it, ignoreCase = true) }
    }



    companion object {
        const val KEY_WORK_TYPE = "key_work_type"
        const val TYPE_PAY_AFTER_BOOKING = "type_pay_after_booking"
        const val TYPE_CREATE_ONLINE_BOOKING = "type_create_online_booking"

        const val KEY_REQUEST_JSON = "key_request_json"
        const val KEY_RESPONSE_JSON = "key_response_json"
        const val KEY_ERROR = "key_error"
        private const val CHANNEL_ID = "jazzcash_payment_channel"
        private const val NOTIFICATION_ID = 4091

        const val KEY_IS_JAZZCASH_ERROR = "is_jazzcash_error"
    }
}
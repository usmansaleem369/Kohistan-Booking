package com.example.kohistanbookingapp.worker

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import androidx.work.workDataOf
import com.example.kohistanbookingapp.models.request.CreateBookingRequest
import com.example.kohistanbookingapp.models.request.JazzCashPayRequest
import com.example.kohistanbookingapp.module.ConnectedNetworkConstraints
import com.google.gson.Gson
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.takeWhile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentWorkScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val gson: Gson,
    @ConnectedNetworkConstraints private val networkConstraints: Constraints
) {
    suspend fun enqueuePayAfterBooking(request: JazzCashPayRequest): WorkInfo? {
        val uniqueWorkName = "jazzcash_pay_${request.bookingTransactionId}"
        return enqueueAndAwait(
            uniqueWorkName = uniqueWorkName,
            workType = JazzCashPaymentWorker.TYPE_PAY_AFTER_BOOKING,
            requestJson = gson.toJson(request)
        )
    }

    suspend fun enqueueCreateOnlineBooking(request: CreateBookingRequest): WorkInfo? {
        val uniqueWorkName =
            "jazzcash_create_${request.routeTimeId}_${request.departureDate}_${request.seats}"
        return enqueueAndAwait(
            uniqueWorkName = uniqueWorkName,
            workType = JazzCashPaymentWorker.TYPE_CREATE_ONLINE_BOOKING,
            requestJson = gson.toJson(request)
        )
    }

    private suspend fun enqueueAndAwait(
        uniqueWorkName: String,
        workType: String,
        requestJson: String
    ): WorkInfo? {
        val workRequest = OneTimeWorkRequestBuilder<JazzCashPaymentWorker>()
            .setInputData(
                workDataOf(
                    JazzCashPaymentWorker.KEY_WORK_TYPE to workType,
                    JazzCashPaymentWorker.KEY_REQUEST_JSON to requestJson
                )
            )
            .setConstraints(networkConstraints)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        workManager.enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.KEEP,
            workRequest
        )

        workManager.getWorkInfosForUniqueWorkFlow(uniqueWorkName)
            .mapNotNull { it.firstOrNull() }
            .takeWhile { !it.state.isFinished }
            .collect { }

        return workManager.getWorkInfosForUniqueWork(uniqueWorkName)
            .await()
            .firstOrNull()
    }
}
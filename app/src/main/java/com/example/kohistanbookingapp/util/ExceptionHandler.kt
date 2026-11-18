package com.example.kohistanbookingapp.util

import retrofit2.HttpException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ExceptionHandler {

    fun getMessage(e: Throwable): String {
        val cause = (e as? java.io.IOException)?.cause ?: e
        return when {
            e is UnknownHostException || cause is UnknownHostException ||
                    e is NoRouteToHostException || cause is NoRouteToHostException -> {
                "No internet connection. Please check your network."
            }

            e is ConnectException || cause is ConnectException -> {
                "Unable to connect to server. Please try again later."
            }

            e is SocketException || cause is SocketException -> {
                "Connection interrupted while waiting for response. Please check your bookings before retrying."
            }

            e is SocketTimeoutException || cause is SocketTimeoutException -> {
                "Connection timed out. Server is taking too long to respond."
            }

            e is HttpException -> {
                when (e.code()) {
                    400 -> "Bad request."
                    401 -> "Unauthorized. Please login again."
                    403 -> "Access denied."
                    404 -> "Data not found."
                    500, 502, 503, 504 -> "Server is currently unavailable. Please try later."
                    else -> "Unexpected server error occurred (${e.code()})."
                }
            }

            else -> {
                "Something went wrong. Please try again."
            }
        }
    }

    fun getPaymentMessage(e: Throwable): String {
        val cause = (e as? java.io.IOException)?.cause ?: e
        return when {
            e is SocketException || cause is SocketException ||
                    e is SocketTimeoutException || cause is SocketTimeoutException -> {
                "Connection interrupted while confirming payment. Please refresh My Bookings to verify status before paying again."
            }
            else -> getMessage(e)
        }
    }
}
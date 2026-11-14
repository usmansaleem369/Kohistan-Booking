package com.example.kohistanbookingapp.util

import retrofit2.HttpException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ExceptionHandler {

    fun getMessage(e: Throwable): String {
        return when (e) {

            is UnknownHostException,
            is NoRouteToHostException -> {
                "No internet connection. Please check your network."
            }

            is ConnectException -> {
                "Unable to connect to server. Please try again later."
            }

            is SocketException -> {
                "Network connection lost. Please check your internet and try again."
            }

            is SocketTimeoutException -> {
                "Connection timed out. Server is taking too long to respond."
            }

            is HttpException -> {
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
}
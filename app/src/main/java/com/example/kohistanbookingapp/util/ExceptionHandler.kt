package com.example.kohistanbookingapp.util

import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ExceptionHandler {
    
    fun getMessage(e: Throwable): String {
        return when (e) {
            
            // No Internet
            is UnknownHostException,
            is ConnectException -> {
                "No internet connection. Please check your network."
            }
            
            // Timeout
            is SocketTimeoutException -> {
                "Connection timed out. Please try again."
            }
            
            // HTTP errors
            is HttpException -> {
                when (e.code()) {
                    400 -> "Bad request."
                    401 -> "Unauthorized. Please login again."
                    403 -> "Access denied."
                    404 -> "Data not found."
                    500 -> "Server error. Please try later."
                    else -> "Unexpected server error occurred."
                }
            }
            
            // Fallback
            else -> {
                "Something went wrong. Please try again."
            }
        }
    }
}

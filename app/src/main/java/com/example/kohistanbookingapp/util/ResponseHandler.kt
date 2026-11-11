package com.example.kohistanbookingapp.util

import com.google.gson.JsonParser
import retrofit2.Response

fun <T> Response<T>.handleResponse(
    onSuccess: (T) -> Unit,
    onError: (String) -> Unit
) {
    if (isSuccessful) {
        body()?.let {
            onSuccess(it)
        } ?: onError("Empty response body")
    } else {
        onError(extractErrorMessage())
    }
}

fun <T> Response<T>.extractErrorMessage(): String {
    return try {
        val errorBody = errorBody()?.string()
        if (errorBody.isNullOrEmpty()) {
            getDefaultMessage(code())
        } else {
            val jsonObject = JsonParser.parseString(errorBody).asJsonObject
            jsonObject.get("error_description")?.asString
                ?: jsonObject.get("Message")?.asString
                ?: jsonObject.get("message")?.asString
                ?:jsonObject.get("error")?.asString
                ?: getDefaultMessage(code())
        }
    } catch (e: Exception) {
        getDefaultMessage(code())
    }
}

private fun getDefaultMessage(code: Int): String {
    return when (code) {
        400 -> "Bad request."
        401 -> "Unauthorized. Please login again."
        403 -> "Access denied."
        404 -> "Data not found."
        500 -> "Server error. Please try later."
        else -> "Unexpected server error occurred."
    }
}

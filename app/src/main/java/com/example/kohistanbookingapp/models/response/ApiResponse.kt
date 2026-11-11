package com.example.kohistanbookingapp.models.response

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("Code") val code: Int? = null,
    @SerializedName("Message") val message: String = "",
    @SerializedName("Data") val data: T? = null,
)

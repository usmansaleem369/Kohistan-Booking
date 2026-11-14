package com.example.kohistanbookingapp.models.request

import com.google.gson.annotations.SerializedName

data class ForgotPasswordRequest(
    @SerializedName("UserNameOrEmail") val userNameOrEmail: String,
)

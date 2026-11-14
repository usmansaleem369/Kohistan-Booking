package com.example.kohistanbookingapp.models.request

import com.google.gson.annotations.SerializedName

data class ResetPasswordRequest(
    @SerializedName("UserNameOrEmail")
    val userNameOrEmail: String,

    @SerializedName("Otp")
    val otp: String,

    @SerializedName("NewPassword")
    val newPassword: String,

    @SerializedName("ConfirmPassword")
    val confirmPassword: String
)
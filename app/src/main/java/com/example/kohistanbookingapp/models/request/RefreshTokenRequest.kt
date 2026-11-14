package com.example.kohistanbookingapp.models.request

import com.google.gson.annotations.SerializedName

data class RefreshTokenRequest(
    @SerializedName("RefreshToken") val refreshToken: String
)
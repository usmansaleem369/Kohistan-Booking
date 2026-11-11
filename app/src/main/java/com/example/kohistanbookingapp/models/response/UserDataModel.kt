package com.example.kohistanbookingapp.models.response

import com.google.gson.annotations.SerializedName

data class UserDataModel(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("client_id") val clientId: String,
    @SerializedName("username") val username: String,
    @SerializedName("fullname") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("contactno") val contactNo: String,
    @SerializedName("employeecode") val employeeCode: String,
    @SerializedName("designation") val designation: String,
    @SerializedName(".issued") val issued: String,
    @SerializedName(".expires") val expires: String
)

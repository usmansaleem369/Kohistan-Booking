package com.example.kohistanbookingapp.models.request

import com.google.gson.annotations.SerializedName

data class CreateProfileRequest(
    @SerializedName("UserName")
    val userName: String,

    @SerializedName("Password")
    val password: String,

    @SerializedName("FirstName")
    val firstName: String,

    @SerializedName("LastName")
    val lastName: String,

    @SerializedName("Gender")
    val gender: String, // "Male" or "Female"

    @SerializedName("Dob")
    val dob: String, // "yyyy-MM-dd" e.g. "1998-03-15"

    @SerializedName("Cnic")
    val cnic: String, // 13 digits without dashes

    @SerializedName("CellNo")
    val cellNo: String, // "03001234567"

    @SerializedName("Email")
    val email: String,

    @SerializedName("Address")
    val address: String,

    @SerializedName("Nationality")
    val nationality: String = "Pakistani"
)
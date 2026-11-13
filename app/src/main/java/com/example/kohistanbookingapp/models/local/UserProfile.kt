package com.example.kohistanbookingapp.models.local

import com.google.gson.annotations.SerializedName

data class UserProfile(
    @SerializedName("Title")
    val title: String, // "Mr", "Mrs", "Miss"

    @SerializedName("FullName")
    val fullName: String,

    @SerializedName("Cnic")
    val cnic: String,

    @SerializedName("Gender")
    val gender: String, // "Male", "Female"

    @SerializedName("PhoneNo")
    val phoneNo: String,

    @SerializedName("Email")
    val email: String? = null,

    @SerializedName("City")
    val city: String? = null
)

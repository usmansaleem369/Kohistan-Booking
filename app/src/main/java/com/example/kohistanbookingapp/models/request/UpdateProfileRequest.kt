package com.example.kohistanbookingapp.models.request

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    @SerializedName("MemberId")
    val memberId: Int,

    @SerializedName("CellNo")
    val cellNo: String,

    /*@SerializedName("PhoneNo")
    val phoneNo: String,*/

    @SerializedName("Email")
    val email: String,

    @SerializedName("Address")
    val address: String,

    @SerializedName("Gender")
    val gender: String
)
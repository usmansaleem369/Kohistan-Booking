package com.example.kohistanbookingapp.models.response

import com.google.gson.annotations.SerializedName

data class ProfileData(
    @SerializedName("MemberId")
    val memberId: Int,

    @SerializedName("UserName")
    val userName: String,

    @SerializedName("FirstName")
    val firstName: String,

    @SerializedName("LastName")
    val lastName: String,

    @SerializedName("Cnic")
    val cnic: String,

    @SerializedName("Dob")
    val dob: String,

    @SerializedName("Gender")
    val gender: String,

    @SerializedName("Nationality")
    val nationality: String,

    @SerializedName("Address")
    val address: String,

    @SerializedName("CellNo")
    val cellNo: String,

    @SerializedName("Email")
    val email: String,

    @SerializedName("MembershipCardNo")
    val membershipCardNo: String? = null,

    @SerializedName("MembershipType")
    val membershipType: String? = null,

    @SerializedName("RoadMiles")
    val roadMiles: Int = 0,

    @SerializedName("StatusMiles")
    val statusMiles: Int = 0,

    @SerializedName("ApplyDateTime")
    val applyDateTime: String? = null
)
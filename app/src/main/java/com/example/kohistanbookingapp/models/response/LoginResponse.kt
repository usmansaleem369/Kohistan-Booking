package com.example.kohistanbookingapp.models.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("AccessToken")
    val accessToken: String?,

    @SerializedName("RefreshToken")
    val refreshToken: String,

    @SerializedName("AccessTokenExpiresAt")
    val accessTokenExpiresAt: String?,

    @SerializedName("MemberId")
    val memberId: Int,

    @SerializedName("MembershipCardNo")
    val membershipCardNo: String?,

    @SerializedName("UserName")
    val userName: String?,

    @SerializedName("FullName")
    val fullName: String?
)

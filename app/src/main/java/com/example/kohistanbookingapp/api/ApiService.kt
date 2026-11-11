package com.example.kohistanbookingapp.api

import com.example.kohistanbookingapp.models.response.ApiResponse
import com.example.kohistanbookingapp.models.response.CityData
import com.example.kohistanbookingapp.models.response.LoginResponse
import com.example.kohistanbookingapp.models.response.RouteData
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @FormUrlEncoded
    @POST("/api/auth/login")
    suspend fun loginUser(
        @Field("UserName") username: String,
        @Field("Password") password: String,
        @Field("DeviceToken") grantType: String = "1223443543545",
    ): Response<ApiResponse<LoginResponse>>

    @POST("/api/auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit?>>


    @GET("/api/City/GetAllDepartureCities")
    suspend fun getAllDepartureCities(): Response<ApiResponse<List<CityData>>>

    @GET("/api/City/GetAllDestinationCitiesIncludingDropPoints")
    suspend fun getAllDestinationCities():Response<ApiResponse<List<CityData>>>


    @GET("/api/RouteTime/GetRouteTimeDetails")
    suspend fun getRouteDetails(@Query("FromCityId") fromCityId: Int,
                                @Query("ToCityId") toCityId: Int,
                                @Query("Date") date: String): Response<ApiResponse<List<RouteData>>>

    
}

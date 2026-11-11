package com.example.kohistanbookingapp.api

import com.example.kohistanbookingapp.models.response.ApiResponse
import com.example.kohistanbookingapp.models.response.CityData
import com.example.kohistanbookingapp.models.response.RouteData
import com.example.kohistanbookingapp.models.response.UserDataModel
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @FormUrlEncoded
    @POST("token")
    suspend fun loginUser(
        @Field("username") username: String,
        @Field("Password") password: String,
        @Field("grant_type") grantType: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("notificationToken") notificationToken: String
    ): Response<UserDataModel>

    @GET("/api/City/GetAll?terminalId=1")
    suspend fun getAllCities(): Response<ApiResponse<List<CityData>>>


    @GET("/api/Route/GetAll?TerminalId=1")
    suspend fun getAllRoutes(): Response<ApiResponse<List<RouteData>>>

    
}

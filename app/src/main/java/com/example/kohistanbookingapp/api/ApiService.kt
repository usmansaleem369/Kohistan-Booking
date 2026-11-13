package com.example.kohistanbookingapp.api

import com.example.kohistanbookingapp.models.request.CreateBookingRequest
import com.example.kohistanbookingapp.models.response.SeatPatternRow
import com.example.kohistanbookingapp.models.response.ApiResponse
import com.example.kohistanbookingapp.models.response.BookedSeatsData
import com.example.kohistanbookingapp.models.response.CityData
import com.example.kohistanbookingapp.models.response.CreateBookingData
import com.example.kohistanbookingapp.models.response.LoginResponse
import com.example.kohistanbookingapp.models.response.RouteData
import retrofit2.Response
import retrofit2.http.Body
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
        @Field("UserType") userType: String = "Member"
    ): Response<ApiResponse<LoginResponse>>

    @POST("/api/auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit?>>


    @GET("/api/City/GetAllDepartureCities")
    suspend fun getAllDepartureCities(): Response<ApiResponse<List<CityData>>>

    @GET("/api/City/GetAllDestinationCitiesIncludingDropPoints")
    suspend fun getAllDestinationCities(): Response<ApiResponse<List<CityData>>>


    @GET("/api/RouteTime/GetRouteTimeDetails")
    suspend fun getRouteDetails(
        @Query("FromCityId") fromCityId: Int,
        @Query("ToCityId") toCityId: Int,
        @Query("Date") date: String
    ): Response<ApiResponse<List<RouteData>>>

    @GET("/api/Seat/GetVehicleSeatPatternByVehicleTypeId?")
    suspend fun getSeatPattern(@Query("vehicleTypeId") vehicleTypeId: Int): Response<ApiResponse<List<SeatPatternRow>>>

    @GET("api/Booking/GetBookedSeats")
    suspend fun getBookedSeats(
        @Query("RouteId") routeId: Int,
        @Query("RouteTimeId") routeTimeId: Long,
        @Query("DepartureDate") departureDate: String, // "yyyy-MM-dd"
        @Query("FromCityId") fromCityId: Int,
        @Query("ToCityId") toCityId: Int
    ): Response<ApiResponse<BookedSeatsData>>

    @POST("api/Booking/CreateBooking")
    suspend fun createBooking(
        @Body request: CreateBookingRequest
    ): Response<ApiResponse<CreateBookingData>>

}

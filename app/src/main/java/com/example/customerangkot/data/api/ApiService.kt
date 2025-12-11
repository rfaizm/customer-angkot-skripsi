package com.example.customerangkot.data.api

import com.example.customerangkot.data.api.dto.FindClosestResponse
import com.example.customerangkot.data.api.dto.GetDriverResponse
import com.example.customerangkot.data.api.dto.GetETAResponse
import com.example.customerangkot.data.api.dto.GetProfileResponse
import com.example.customerangkot.data.api.dto.HistoryResponse
import com.example.customerangkot.data.api.dto.LoginSuccessResponse
import com.example.customerangkot.data.api.dto.LogoutResponse
import com.example.customerangkot.data.api.dto.OrderCancelResponse
import com.example.customerangkot.data.api.dto.OrderCreatedResponse
import com.example.customerangkot.data.api.dto.PlaceNameResponse
import com.example.customerangkot.data.api.dto.PlaceToCoordinateResponse
import com.example.customerangkot.data.api.dto.RegisterSuccessResponse
import com.example.customerangkot.data.api.dto.RouteResponse
import com.example.customerangkot.data.api.dto.TopUpResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

interface ApiService {

    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("name") name : String,
        @Field("email") email : String,
        @Field("no_hp") numberPhone : String,
        @Field("password") password : String
    ): Response<RegisterSuccessResponse>

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email : String,
        @Field("password") password : String
    ) : Response<LoginSuccessResponse>


    @POST("logout")
    suspend fun logout(
        @Header("Authorization") token: String,
    ): LogoutResponse

    @FormUrlEncoded
    @POST("trayek_closest")
    suspend fun trayekClosest(
        @Header("Authorization") token: String,
        @Field("lat") lat : Double,
        @Field("long") long : Double
    ) : FindClosestResponse

    @FormUrlEncoded
    @POST("show_angkot_location")
    suspend fun showAngkotLocationByIdTrayek(
        @Header("Authorization") token: String,
        @Field("trayek_id") trayekId : Int,
        @Field("lat") lat : Double,
        @Field("long") long : Double
    ) : FindClosestResponse

    @FormUrlEncoded
    @POST("place-name")
    suspend fun placeName(
        @Header("Authorization") token: String,
        @Field("lat") lat : Double,
        @Field("long") long : Double
    ) : PlaceNameResponse

    @FormUrlEncoded
    @POST("place-to-coordinates")
    suspend fun placeToCoordinates(
        @Header("Authorization") token: String,
        @Field("place") place : String,
        @Field("user_lat") lat : Double,
        @Field("user_long") long : Double
    ) : PlaceToCoordinateResponse

    @FormUrlEncoded
    @POST("routes")
    suspend fun routes(
        @Header("Authorization") token: String,
        @Field("start_lat") startLat : Double,
        @Field("start_long") startLong : Double,
        @Field("end_lat") destinationLat : Double,
        @Field("end_long") destinationLong : Double,
        @Field("route_option") routeOption : String
    ) : RouteResponse

    @FormUrlEncoded
    @POST("create-order")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Field("driver_id") driverId : Int,
        @Field("starting_point_lat") startLat : Double,
        @Field("starting_point_long") startLong : Double,
        @Field("destination_point_lat") destinationLat : Double,
        @Field("destination_point_long") destinationLong : Double,
        @Field("number_of_passengers") numberOfPassengers : Int,
        @Field("price") totalPrice : Int,
        @Field("payment_method") paymentMethod : String
    ) : Response<OrderCreatedResponse>

    @FormUrlEncoded
    @PATCH("topup")
    suspend fun topUp(
        @Header("Authorization") token: String,
        @Field("nominal") nominal: Int
    ): TopUpResponse

    @GET("saldo")
    suspend fun getSaldo(
        @Header("Authorization") token: String
    ): TopUpResponse

    @GET("history")
    suspend fun getHistory(
        @Header("Authorization") token: String
    ): HistoryResponse

    @GET("profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ) : GetProfileResponse

    @FormUrlEncoded
    @PATCH("cancel-order")
    suspend fun cancelOrder(
        @Header("Authorization") token: String,
        @Field("order_id") orderId : Int
    ) : OrderCancelResponse

    @FormUrlEncoded
    @POST("get-eta")
    suspend fun getEta(
        @Header("Authorization") token: String,
        @Field("start_lat") startLat : Double,
        @Field("start_long") startLong : Double,
        @Field("end_lat") endLat : Double,
        @Field("end_long") endLong : Double,
    ) : GetETAResponse

    @FormUrlEncoded
    @POST("get-driver")
    suspend fun getDriver(
        @Header("Authorization") token: String,
        @Field("angkot_id") angkotId : Int,
    ) : Response<GetDriverResponse>
}
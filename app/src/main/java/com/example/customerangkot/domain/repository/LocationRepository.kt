package com.example.customerangkot.domain.repository

import com.example.customerangkot.data.api.dto.PlaceNameResponse
import com.example.customerangkot.data.api.dto.PlaceToCoordinateResponse
import com.example.customerangkot.data.api.dto.RouteResponse
import com.example.customerangkot.domain.entity.LatLng

interface LocationRepository {
    suspend fun getLastLocation(): LatLng?
    suspend fun getNamePlace(token: String, lat: Double, lng: Double) : PlaceNameResponse
    suspend fun searchPlaceToCoordinates(token: String, place: String, userLat: Double, userLng: Double): PlaceToCoordinateResponse
    suspend fun getRoutes(token: String, startLat: Double, startLong: Double, endLat: Double, endLong: Double, routeOption: String): RouteResponse
}
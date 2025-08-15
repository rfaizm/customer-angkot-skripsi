package com.example.customerangkot.data.datasource

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.example.customerangkot.data.api.ApiService
import com.example.customerangkot.data.api.dto.PlaceNameResponse
import com.example.customerangkot.data.api.dto.PlaceToCoordinateResponse
import com.example.customerangkot.data.api.dto.RouteResponse
import com.example.customerangkot.domain.entity.LatLng
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await

class LocationDataSourceImpl(
    private val context: Context,
    private val apiService: ApiService
) : LocationDataSource {

    private val TAG = "LocationDataSourceImpl"
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    override suspend fun getLastLocation(): LatLng? {
        try {
            Log.d(TAG, "Fetching last location")
            val location = fusedLocationClient.lastLocation.await()
            return if (location != null) {
                LatLng(location.latitude, location.longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching last location: ${e.message}", e)
            return null
        }
    }

    override suspend fun getNamePlace(token: String, lat: Double, lng: Double): PlaceNameResponse {
        try {
            Log.d(TAG, "Fetching place name with lat=$lat, lng=$lng")
            return apiService.placeName("Bearer $token", lat, lng)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching place name: ${e.message}", e)
            throw e
        }
    }

    override suspend fun searchPlaceToCoordinates(token: String, place: String, userLat: Double, userLng: Double): PlaceToCoordinateResponse {
        try {
            Log.d(TAG, "Searching coordinates for place=$place, userLat=$userLat, userLng=$userLng")
            return apiService.placeToCoordinates("Bearer $token", place, userLat, userLng)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching coordinates: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getRoutes(token: String, startLat: Double, startLong: Double, endLat: Double, endLong: Double, routeOption: String): RouteResponse {
        try {
            Log.d(TAG, "Fetching routes: startLat=$startLat, startLong=$startLong, endLat=$endLat, endLong=$endLong")
            return apiService.routes("Bearer $token", startLat, startLong, endLat, endLong, routeOption)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching routes: ${e.message}", e)
            throw e
        }
    }
}
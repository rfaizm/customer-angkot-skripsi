package com.example.customerangkot.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.example.customerangkot.data.api.ApiService
import com.example.customerangkot.data.api.dto.PlaceNameResponse
import com.example.customerangkot.data.api.dto.PlaceToCoordinateResponse
import com.example.customerangkot.data.api.dto.RouteResponse
import com.example.customerangkot.data.datasource.LocationDataSource
import com.example.customerangkot.domain.entity.LatLng
import com.example.customerangkot.domain.repository.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException

class LocationRepositoryImpl(
    private val locationDataSource: LocationDataSource
) : LocationRepository {

    private val TAG = "LocationRepository"

    override suspend fun getLastLocation(): LatLng? {
        try {
            Log.d(TAG, "Fetching last location from DataSource")
            return locationDataSource.getLastLocation()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching last location: ${e.message}", e)
            return null
        }
    }

    override suspend fun getNamePlace(token: String, lat: Double, lng: Double): PlaceNameResponse {
        try {
            Log.d(TAG, "Fetching place name from DataSource with lat=$lat, lng=$lng")
            return locationDataSource.getNamePlace(token, lat, lng)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching place name: ${e.message}", e)
            throw e
        }
    }

    override suspend fun searchPlaceToCoordinates(token: String, place: String, userLat: Double, userLng: Double): PlaceToCoordinateResponse {
        try {
            Log.d(TAG, "Searching coordinates from DataSource for place=$place, userLat=$userLat, userLng=$userLng")
            return locationDataSource.searchPlaceToCoordinates(token, place, userLat, userLng)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching coordinates: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getRoutes(
        token: String,
        startLat: Double,
        startLong: Double,
        endLat: Double,
        endLong: Double,
        routeOption: String
    ): RouteResponse {
        try {
            Log.d(TAG, "Fetching routes from DataSource: startLat=$startLat, startLong=$startLong, endLat=$endLat, endLong=$endLong")
            return locationDataSource.getRoutes(token, startLat, startLong, endLat, endLong, routeOption)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching routes: ${e.message}", e)
            throw e
        }
    }
}
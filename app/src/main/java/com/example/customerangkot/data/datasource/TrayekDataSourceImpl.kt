package com.example.customerangkot.data.datasource

import android.util.Log
import com.example.customerangkot.data.api.ApiService
import com.example.customerangkot.data.api.dto.FindClosestResponse

class TrayekDataSourceImpl(
    private val apiService: ApiService
) : TrayekDataSource {

    private val TAG = "TrayekDataSourceImpl"

    override suspend fun getClosestTrayek(token: String, lat: Double, lng: Double): FindClosestResponse {
        try {
            Log.d(TAG, "Fetching closest trayek with lat=$lat, lng=$lng")
            return apiService.trayekClosest("Bearer $token", lat, lng)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching closest trayek: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getAllAngkotByIdTrayek(token: String, lat: Double, lng: Double, trayekId: Int): FindClosestResponse {
        try {
            Log.d(TAG, "Fetching angkot by trayekId=$trayekId with lat=$lat, lng=$lng")
            return apiService.showAngkotLocationByIdTrayek("Bearer $token", trayekId, lat, lng)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching angkot by trayekId: ${e.message}", e)
            throw e
        }
    }
}
package com.example.customerangkot.data.repository

import android.util.Log
import com.example.customerangkot.data.api.ApiService
import com.example.customerangkot.data.api.dto.FindClosestResponse
import com.example.customerangkot.data.datasource.TrayekDataSource
import com.example.customerangkot.domain.repository.TrayekRepository
import retrofit2.HttpException

class TrayekRepositoryImpl(
    private val trayekDataSource: TrayekDataSource
) : TrayekRepository {

    private val TAG = "TrayekRepositoryImpl"

    override suspend fun getClosestTrayek(token: String, lat: Double, lng: Double): FindClosestResponse {
        try {
            Log.d(TAG, "Fetching closest trayek with lat=$lat, lng=$lng")
            return trayekDataSource.getClosestTrayek(token, lat, lng)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching closest trayek: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getAllAngkotByIdTrayek(token: String, lat: Double, lng: Double, trayekId: Int): FindClosestResponse {
        try {
            Log.d(TAG, "Fetching angkot by trayekId=$trayekId with lat=$lat, lng=$lng")
            return trayekDataSource.getAllAngkotByIdTrayek(token, lat, lng, trayekId)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching angkot by trayekId: ${e.message}", e)
            throw e
        }
    }
}
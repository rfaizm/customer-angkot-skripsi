package com.example.customerangkot.data.datasource

import android.util.Log
import com.example.customerangkot.data.api.ApiService
import com.example.customerangkot.data.api.dto.GetETAResponse
import com.example.customerangkot.data.api.dto.OrderCancelResponse
import com.example.customerangkot.data.api.dto.OrderCreatedResponse
import com.example.customerangkot.data.preference.UserPreference

class OrderDataSourceImpl(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) : OrderDataSource {

    private val TAG = "OrderDataSourceImpl"

    override suspend fun createOrder(
        token: String,
        driverId: Int,
        startLat: Double,
        startLong: Double,
        destinationLat: Double,
        destinationLong: Double,
        numberOfPassengers: Int,
        totalPrice: Double
    ): OrderCreatedResponse {
        try {
            Log.d(TAG, "Creating order with driverId=$driverId, totalPrice=$totalPrice")
            return apiService.createOrder(
                "Bearer $token",
                driverId,
                startLat,
                startLong,
                destinationLat,
                destinationLong,
                numberOfPassengers,
                totalPrice.toInt()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating order: ${e.message}", e)
            throw e
        }
    }

    override suspend fun cancelOrder(token: String, orderId: Int): OrderCancelResponse {
        try {
            Log.d(TAG, "Canceling order with orderId=$orderId")
            return apiService.cancelOrder("Bearer $token", orderId)
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling order: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getETA(
        token: String,
        startLat: Double,
        startLong: Double,
        endLat: Double,
        endLong: Double
    ): GetETAResponse {
        try {
            Log.d(TAG, "Fetching ETA with startLat=$startLat, startLong=$startLong, endLat=$endLat, endLong=$endLong")
            return apiService.getEta("Bearer $token", startLat, startLong, endLat, endLong)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching ETA: ${e.message}", e)
            throw e
        }
    }
}
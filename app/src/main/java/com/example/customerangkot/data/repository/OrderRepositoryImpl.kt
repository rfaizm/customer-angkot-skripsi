package com.example.customerangkot.data.repository

import android.util.Log
import com.example.customerangkot.data.api.ApiService
import com.example.customerangkot.data.api.dto.GetETAResponse
import com.example.customerangkot.data.api.dto.OrderCancelResponse
import com.example.customerangkot.data.api.dto.OrderCreatedResponse
import com.example.customerangkot.data.datasource.OrderDataSource
import com.example.customerangkot.data.preference.UserPreference
import com.example.customerangkot.domain.repository.OrderRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.HttpException

class OrderRepositoryImpl(
    private val orderDataSource: OrderDataSource
) : OrderRepository {

    private val TAG = "OrderRepositoryImpl"

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
            return orderDataSource.createOrder(token, driverId, startLat, startLong, destinationLat, destinationLong, numberOfPassengers, totalPrice)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating order: ${e.message}", e)
            throw e
        }
    }

    override suspend fun cancelOrder(token: String, orderId: Int): OrderCancelResponse {
        try {
            Log.d(TAG, "Canceling order with orderId=$orderId")
            return orderDataSource.cancelOrder(token, orderId)
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
            return orderDataSource.getETA(token, startLat, startLong, endLat, endLong)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching ETA: ${e.message}", e)
            throw e
        }
    }
}
package com.example.customerangkot.domain.repository

import com.example.customerangkot.data.api.dto.GetETAResponse
import com.example.customerangkot.data.api.dto.OrderCancelResponse
import com.example.customerangkot.data.api.dto.OrderCreatedResponse

interface OrderRepository {
    suspend fun createOrder(
        token: String,
        driverId: Int,
        startLat: Double,
        startLong: Double,
        destinationLat: Double,
        destinationLong: Double,
        numberOfPassengers: Int,
        totalPrice: Double
    ): OrderCreatedResponse

    suspend fun cancelOrder(token: String, orderId: Int): OrderCancelResponse // [Baru]
    suspend fun getETA(
        token: String,
        startLat: Double,
        startLong: Double,
        endLat: Double,
        endLong: Double
    ): GetETAResponse // [Baru]
}
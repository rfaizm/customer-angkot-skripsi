package com.example.customerangkot.data.datasource

import com.example.customerangkot.data.api.dto.CheckOrderActiveResponse
import com.example.customerangkot.data.api.dto.GetETAResponse
import com.example.customerangkot.data.api.dto.OrderCancelResponse
import com.example.customerangkot.data.api.dto.OrderCreatedResponse

interface OrderDataSource {
    suspend fun createOrder(
        token: String,
        driverId: Int,
        startLat: Double,
        startLong: Double,
        destinationLat: Double,
        destinationLong: Double,
        numberOfPassengers: Int,
        totalPrice: Double,
        methodPayment: String,
        polyline: String
    ): OrderCreatedResponse
    suspend fun cancelOrder(token: String, orderId: Int): OrderCancelResponse
    suspend fun getETA(
        token: String,
        startLat: Double,
        startLong: Double,
        endLat: Double,
        endLong: Double
    ): GetETAResponse

    suspend fun getCheckOrderActive(token: String) : CheckOrderActiveResponse

}
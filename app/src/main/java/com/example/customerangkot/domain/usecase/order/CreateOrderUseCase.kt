package com.example.customerangkot.domain.usecase.order

import android.util.Log
import com.example.customerangkot.data.api.dto.OrderCreatedResponse
import com.example.customerangkot.data.preference.UserPreference
import com.example.customerangkot.domain.repository.OrderRepository

class CreateOrderUseCase(
    private val orderRepository: OrderRepository,
    private val userPreference: UserPreference
) {
    suspend operator fun invoke(
        driverId: Int,
        startLat: Double,
        startLong: Double,
        destinationLat: Double,
        destinationLong: Double,
        numberOfPassengers: Int,
        totalPrice: Double,
        methodPayment: String
    ): Result<OrderCreatedResponse> {
        return try {
            val token = userPreference.getAuthToken()
                ?: return Result.failure(Exception("Token tidak valid"))

            val response = orderRepository.createOrder(
                token = token,
                driverId = driverId,
                startLat = startLat,
                startLong = startLong,
                destinationLat = destinationLat,
                destinationLong = destinationLong,
                numberOfPassengers = numberOfPassengers,
                totalPrice = totalPrice,
                methodPayment = methodPayment
            )
            Result.success(response)
        } catch (e: Exception) {
            // [FIX] Kirim pesan asli dari backend
            Result.failure(e)
        }
    }
}
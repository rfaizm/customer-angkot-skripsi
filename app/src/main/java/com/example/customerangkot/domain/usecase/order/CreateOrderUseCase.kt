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
        totalPrice: Double
    ): Result<OrderCreatedResponse> {
        return try {
            Log.d("CreateOrderUseCase", "Memulai pembuatan pesanan: driverId=$driverId, startLat=$startLat, startLong=$startLong, destinationLat=$destinationLat, destinationLong=$destinationLong, numberOfPassengers=$numberOfPassengers, totalPrice=$totalPrice")
            val token = userPreference.getAuthToken()
            if (token == null) {
                Log.e("CreateOrderUseCase", "Token tidak ditemukan")
                return Result.failure(Exception("Token tidak valid"))
            }
            Log.d("CreateOrderUseCase", "Menggunakan token: $token")
            val response = orderRepository.createOrder(
                token = token,
                driverId = driverId,
                startLat = startLat,
                startLong = startLong,
                destinationLat = destinationLat,
                destinationLong = destinationLong,
                numberOfPassengers = numberOfPassengers,
                totalPrice = totalPrice
            )
            Log.d("CreateOrderUseCase", "Pesanan berhasil dibuat: orderId=${response.data?.orderId}")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("CreateOrderUseCase", "Error: ${e.message}", e)
            Result.failure(Exception("Gagal membuat pesanan: ${e.message}"))
        }
    }
}
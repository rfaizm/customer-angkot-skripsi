package com.example.customerangkot.domain.usecase.order

import android.util.Log
import com.example.customerangkot.data.api.dto.OrderCancelResponse
import com.example.customerangkot.data.preference.UserPreference
import com.example.customerangkot.di.ResultState
import com.example.customerangkot.domain.repository.OrderRepository

// [Baru] Use case untuk membatalkan pesanan
class CancelOrderUseCase(
    private val orderRepository: OrderRepository,
    private val userPreference: UserPreference
) {
    suspend operator fun invoke(orderId: Int): Result<OrderCancelResponse> {
        return try {
            Log.d("CancelOrderUseCase", "Memulai pembatalan pesanan: orderId=$orderId")
            val token = userPreference.getAuthToken()
            if (token == null) {
                Log.e("CancelOrderUseCase", "Token tidak ditemukan")
                return Result.failure(Exception("Token tidak valid"))
            }
            Log.d("CancelOrderUseCase", "Menggunakan token: $token")
            val response = orderRepository.cancelOrder("Bearer $token", orderId)
            Log.d("CancelOrderUseCase", "Pesanan berhasil dibatalkan: message=${response.message}")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("CancelOrderUseCase", "Error: ${e.message}", e)
            Result.failure(Exception("Gagal membatalkan pesanan: ${e.message}"))
        }
    }
}
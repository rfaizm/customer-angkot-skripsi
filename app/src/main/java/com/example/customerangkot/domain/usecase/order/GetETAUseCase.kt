package com.example.customerangkot.domain.usecase.order

import android.util.Log
import com.example.customerangkot.data.api.dto.GetETAResponse
import com.example.customerangkot.data.preference.UserPreference
import com.example.customerangkot.domain.repository.OrderRepository

class GetETAUseCase(
    private val orderRepository: OrderRepository,
    private val userPreference: UserPreference
) {
    suspend operator fun invoke(
        startLat: Double,
        startLong: Double,
        endLat: Double,
        endLong: Double
    ): Result<GetETAResponse> {
        return try {
            Log.d("GetETAUseCase", "Fetching ETA: startLat=$startLat, startLong=$startLong, endLat=$endLat, endLong=$endLong")
            val token = userPreference.getAuthToken()
            if (token == null) {
                Log.e("GetETAUseCase", "Token tidak ditemukan")
                return Result.failure(Exception("Token tidak valid"))
            }
            Log.d("GetETAUseCase", "Menggunakan token: $token")
            val response = orderRepository.getETA(
                token = "Bearer $token",
                startLat = startLat,
                startLong = startLong,
                endLat = endLat,
                endLong = endLong
            )
            Log.d("GetETAUseCase", "ETA berhasil: eta=${response.data?.eta}")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("GetETAUseCase", "Error: ${e.message}", e)
            Result.failure(Exception("Gagal mendapatkan ETA: ${e.message}"))
        }
    }
}
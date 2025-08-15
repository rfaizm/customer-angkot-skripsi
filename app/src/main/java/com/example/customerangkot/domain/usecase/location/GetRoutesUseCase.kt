package com.example.customerangkot.domain.usecase.location

import android.util.Log
import com.example.customerangkot.data.api.dto.RouteResponse
import com.example.customerangkot.data.preference.UserPreference
import com.example.customerangkot.domain.repository.LocationRepository

class GetRoutesUseCase(
    private val locationRepository: LocationRepository,
    private val userPreference: UserPreference
) {
    suspend operator fun invoke(
        startLat: Double,
        startLong: Double,
        endLat: Double,
        endLong: Double,
        routeOption: String
    ): Result<RouteResponse> {
        return try {
            Log.d("GetRoutesUseCase", "Memulai pengambilan rute untuk startLat=$startLat, startLong=$startLong, endLat=$endLat, endLong=$endLong, routeOption=$routeOption")
            val token = userPreference.getAuthToken()
            if (token == null) {
                Log.e("GetRoutesUseCase", "Token tidak ditemukan")
                return Result.failure(Exception("Token tidak ditemukan"))
            }
            Log.d("GetRoutesUseCase", "Menggunakan token: $token")
            val response = locationRepository.getRoutes(token, startLat, startLong, endLat, endLong, routeOption)
            if (response.status == "success") {
                Log.d("GetRoutesUseCase", "Rute berhasil diambil: ${response.data?.size} rute")
                Result.success(response)
            } else {
                Log.e("GetRoutesUseCase", "Gagal mengambil rute: ${response.status}")
                Result.failure(Exception("Gagal mengambil rute"))
            }
        } catch (e: Exception) {
            Log.e("GetRoutesUseCase", "Error: ${e.message}", e)
            Result.failure(Exception("Gagal mengambil rute: ${e.message}"))
        }
    }
}
package com.example.customerangkot.domain.usecase.location

import android.util.Log
import com.example.customerangkot.data.api.dto.PlaceToCoordinateResponse
import com.example.customerangkot.data.preference.UserPreference
import com.example.customerangkot.domain.repository.LocationRepository

class SearchPlaceToCoordinatesUseCase(
    private val locationRepository: LocationRepository,
    private val userPreference: UserPreference
) {
    suspend operator fun invoke(place: String, userLat: Double, userLng: Double): Result<PlaceToCoordinateResponse> {
        return try {
            Log.d("SearchPlaceToCoordinatesUseCase", "Mencari tempat: $place, userLat: $userLat, userLng: $userLng")
            val token = userPreference.getAuthToken()
            if (token == null) {
                Log.e("SearchPlaceToCoordinatesUseCase", "Token tidak ditemukan")
                return Result.failure(Exception("Token tidak ditemukan"))
            }
            Log.d("SearchPlaceToCoordinatesUseCase", "Menggunakan token: $token")
            val response = locationRepository.searchPlaceToCoordinates(token, place, userLat, userLng)
            if (response.status == "success") {
                Log.d("SearchPlaceToCoordinatesUseCase", "Pencarian berhasil: ${response.data?.size} hasil")
                Result.success(response)
            } else {
                Log.e("SearchPlaceToCoordinatesUseCase", "Gagal mencari tempat: ${response.status}")
                Result.failure(Exception("Gagal mencari tempat"))
            }
        } catch (e: Exception) {
            Log.e("SearchPlaceToCoordinatesUseCase", "Error: ${e.message}", e)
            Result.failure(Exception("Gagal mencari tempat: ${e.message}"))
        }
    }
}
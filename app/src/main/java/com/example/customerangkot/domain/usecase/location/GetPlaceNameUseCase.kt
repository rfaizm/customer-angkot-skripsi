package com.example.customerangkot.domain.usecase.location

import com.example.customerangkot.data.api.dto.PlaceNameResponse
import com.example.customerangkot.data.preference.UserPreference
import com.example.customerangkot.domain.repository.LocationRepository

class GetPlaceNameUseCase(
    private val locationRepository: LocationRepository,
    private val userPreference: UserPreference
) {
    suspend operator fun invoke(lat: Double, lng: Double): Result<PlaceNameResponse> {
        return try {
            val token = userPreference.getAuthToken()
                ?: return Result.failure(Exception("Token tidak ditemukan"))
            val response = locationRepository.getNamePlace(token, lat, lng)
            if (response.status == "success") {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message ?: "Gagal mengambil nama tempat"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
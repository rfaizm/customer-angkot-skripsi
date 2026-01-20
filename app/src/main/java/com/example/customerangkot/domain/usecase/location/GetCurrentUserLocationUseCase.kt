package com.example.customerangkot.domain.usecase.location

import com.example.customerangkot.domain.entity.LatLng
import com.example.customerangkot.domain.repository.LocationRepository

class GetCurrentUserLocationUseCase(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(): Result<LatLng> {
        return try {
            val location = locationRepository.getCurrentLocation()
            if (location != null) {
                Result.success(location)
            } else {
                Result.failure(Exception("Tidak dapat mendapatkan lokasi terbaru"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

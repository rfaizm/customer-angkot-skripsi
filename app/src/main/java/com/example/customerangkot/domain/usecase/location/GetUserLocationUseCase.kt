package com.example.customerangkot.domain.usecase.location

import com.example.customerangkot.domain.entity.LatLng
import com.example.customerangkot.domain.repository.LocationRepository

class GetUserLocationUseCase(private val locationRepository: LocationRepository) {
    suspend operator fun invoke(): Result<LatLng> {
        return try {
            val location = locationRepository.getLastLocation()
            if (location != null) {
                Result.success(location)
            } else {
                Result.failure(Exception("Tidak dapat mendapatkan lokasi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
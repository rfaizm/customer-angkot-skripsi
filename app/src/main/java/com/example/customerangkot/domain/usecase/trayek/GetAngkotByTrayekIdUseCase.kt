package com.example.customerangkot.domain.usecase.trayek

import com.example.customerangkot.data.api.dto.DataTrayekJSON
import com.example.customerangkot.data.preference.UserPreference
import com.example.customerangkot.domain.repository.TrayekRepository

class GetAngkotByTrayekIdUseCase(
    private val trayekRepository: TrayekRepository,
    private val userPreference: UserPreference
) {
    suspend operator fun invoke(lat: Double, lng: Double, trayekId: Int): Result<List<DataTrayekJSON>> {
        return try {
            val token = userPreference.getAuthToken()
                ?: return Result.failure(Exception("Token tidak ditemukan"))
            val response = trayekRepository.getAllAngkotByIdTrayek(token, lat, lng, trayekId)
            if (response.status == "success" && response.data != null) {
                Result.success(response.data.filterNotNull())
            } else {
                Result.failure(Exception(response.message ?: "Gagal mengambil data angkot"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
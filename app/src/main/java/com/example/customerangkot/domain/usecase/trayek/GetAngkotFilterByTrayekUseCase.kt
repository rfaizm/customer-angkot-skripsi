package com.example.customerangkot.domain.usecase.trayek

import android.util.Log
import com.example.customerangkot.data.api.dto.DataAngkotFilter
import com.example.customerangkot.data.preference.UserPreference
import com.example.customerangkot.domain.repository.TrayekRepository

class GetAngkotFilterByTrayekUseCase(
    private val repository: TrayekRepository,
    private val userPreference: UserPreference
) {
    suspend operator fun invoke(
        trayekId: Int,
        lat: Double,
        lng: Double,
        polyline: String
    ): Result<List<DataAngkotFilter>> {
        return try {
            Log.d("GetAngkotFilterByTrayekUseCase", "trayekId: $trayekId, lat: $lat, lng: $lng, polyline: $polyline")
            val token = userPreference.getAuthToken()
                ?: return Result.failure(Exception("Token tidak ditemukan"))
            val response = repository.getAngkotFilterByTrayek(token, trayekId, lat, lng, polyline)

            if (response.status == "success" && response.data != null) {
                Log.d("GetAngkotFilterByTrayekUseCase", "DataAngkotFilter: ${response.data}")
                Result.success(response.data.filterNotNull())
            } else {
                Result.failure(Exception(response.message ?: "Gagal mengambil data angkot"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

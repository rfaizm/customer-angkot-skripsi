package com.example.customerangkot.domain.usecase.trayek

import android.util.Log
import com.example.customerangkot.data.api.dto.GetDriverResponse
import com.example.customerangkot.data.preference.UserPreference
import com.example.customerangkot.domain.repository.TrayekRepository

class GetDriverIdWithAngkotIdUseCase(
    private val trayekRepository: TrayekRepository,
    private val userPreference: UserPreference
) {
    suspend operator fun invoke(angkotId: Int) : Result<GetDriverResponse> {
        return try {
            val token = userPreference.getAuthToken()
            if (token == null) {
                Log.e("GetDriverIdWithAngkotId", "Token tidak ditemukan")
                return Result.failure(Exception("Token tidak valid"))
            }
            Log.d("GetDriverIdWithAngkotId", "Menggunakan token: $token")
            val response = trayekRepository.getIdDriverWithAngkotId(token, angkotId)

            Result.success(response)
        } catch (e: Exception) {
            Log.e("GetDriverIdWithAngkotIdUseCase", "Error: ${e.message}", e)
            Result.failure(Exception("Gagal Untuk GetDriverIdWithAngkotIdUseCase: ${e.message}"))
        }
    }
}
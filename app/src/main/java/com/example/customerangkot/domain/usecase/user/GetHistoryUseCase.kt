package com.example.customerangkot.domain.usecase.user

import android.util.Log
import com.example.customerangkot.data.api.dto.HistoryResponse
import com.example.customerangkot.domain.repository.UserRepository

class GetHistoryUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<HistoryResponse> {
        return try {
            Log.d("GetHistoryUseCase", "Memulai pengambilan riwayat")
            val response = userRepository.getHistory()
            Log.d("GetHistoryUseCase", "Riwayat berhasil diambil: ${response.message}")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("GetHistoryUseCase", "Error: ${e.message}", e)
            Result.failure(Exception("Gagal mengambil riwayat: ${e.message}"))
        }
    }
}
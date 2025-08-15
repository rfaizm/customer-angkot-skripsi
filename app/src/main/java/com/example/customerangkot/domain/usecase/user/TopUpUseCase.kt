package com.example.customerangkot.domain.usecase.user

import android.util.Log
import com.example.customerangkot.data.api.dto.TopUpResponse
import com.example.customerangkot.domain.repository.UserRepository

class TopUpUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(nominal: Int): Result<TopUpResponse> {
        return try {
            Log.d("TopUpUseCase", "Memulai top-up dengan nominal: $nominal")
            val response = userRepository.topUp(nominal)
            Log.d("TopUpUseCase", "Top-up berhasil: ${response.message}")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("TopUpUseCase", "Error top-up: ${e.message}", e)
            Result.failure(Exception("Gagal top-up: ${e.message}"))
        }
    }
}
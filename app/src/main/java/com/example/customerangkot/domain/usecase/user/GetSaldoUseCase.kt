package com.example.customerangkot.domain.usecase.user

import android.util.Log
import com.example.customerangkot.data.api.dto.TopUpResponse
import com.example.customerangkot.domain.repository.UserRepository

class GetSaldoUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<TopUpResponse> {
        return try {
            val response = userRepository.getSaldo()
            Log.d("GetSaldoUseCase", "Get Saldo berhasil: ${response.message}")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("GetSaldoUseCase", "Error get saldo: ${e.message}", e)
            Result.failure(Exception("Gagal get saldo: ${e.message}"))
        }
    }
}
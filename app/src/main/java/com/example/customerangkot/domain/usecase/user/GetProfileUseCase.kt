package com.example.customerangkot.domain.usecase.user

import android.util.Log
import com.example.customerangkot.data.api.dto.GetProfileResponse
import com.example.customerangkot.domain.repository.UserRepository

class GetProfileUseCase (
    private val userRepository: UserRepository
){
    suspend operator fun invoke(): Result<GetProfileResponse> {
        return try {
            val response = userRepository.getProfile()
            Log.d("GetProfileUseCase", "Get Profile berhasil: ${response.message}")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("TopUpUseCase", "Error top-up: ${e.message}", e)
            Result.failure(Exception("Gagal top-up: ${e.message}"))
        }
    }
}
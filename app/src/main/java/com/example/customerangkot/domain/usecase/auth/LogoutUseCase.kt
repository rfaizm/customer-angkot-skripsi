package com.example.customerangkot.domain.usecase.auth

import com.example.customerangkot.data.api.dto.LogoutResponse
import com.example.customerangkot.domain.repository.UserRepository

class LogoutUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(): Result<LogoutResponse> {
        return try {
            val response = userRepository.logout()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
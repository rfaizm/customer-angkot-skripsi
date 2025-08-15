package com.example.customerangkot.domain.usecase.auth

import com.example.customerangkot.domain.entity.Passenger
import com.example.customerangkot.domain.entity.User
import com.example.customerangkot.domain.repository.UserRepository

class LoginUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Pair<User, Passenger>> {
        // Validasi input
        if (email.isBlank() || !email.contains("@") || !email.contains(".")) {
            return Result.failure(Exception("Email tidak valid"))
        }
        if (password.length < 8) {
            return Result.failure(Exception("Password harus minimal 8 karakter"))
        }

        return try {
            val (user, passenger, token) = userRepository.login(email, password)
            userRepository.saveSession(
                user = user,
                token = token,
                noHp = passenger.noHp,
                saldo = passenger.saldo
            )
            Result.success(Pair(user, passenger))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
package com.example.customerangkot.domain.usecase.auth

import com.example.customerangkot.domain.entity.Passenger
import com.example.customerangkot.domain.entity.User
import com.example.customerangkot.domain.repository.UserRepository

class LoginUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Pair<User, Passenger>> {
        // [HAPUS] Validasi email/password → biarkan backend tangani
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
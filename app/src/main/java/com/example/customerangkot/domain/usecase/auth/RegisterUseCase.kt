package com.example.customerangkot.domain.usecase.auth

import com.example.customerangkot.domain.entity.Passenger
import com.example.customerangkot.domain.entity.User
import com.example.customerangkot.domain.repository.UserRepository

class RegisterUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(
        name: String,
        email: String,
        noHp: String,
        password: String
    ): Result<Pair<User, Passenger>> {
        // Validasi input
        if (name.isBlank()) {
            return Result.failure(Exception("Nama lengkap tidak boleh kosong"))
        }
        if (email.isBlank() || !email.contains("@") || !email.contains(".")) {
            return Result.failure(Exception("Email tidak valid"))
        }
        if (noHp.isBlank() || !noHp.matches(Regex("^[0-9]{10,13}$"))) {
            return Result.failure(Exception("Nomor HP tidak valid (harus 10-13 digit angka)"))
        }
        if (password.length < 8) {
            return Result.failure(Exception("Password harus minimal 8 karakter"))
        }

        return try {
            val result = userRepository.register(name, email, noHp, password)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
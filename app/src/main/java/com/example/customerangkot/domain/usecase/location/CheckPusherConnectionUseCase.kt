package com.example.customerangkot.domain.usecase.location

import com.example.customerangkot.domain.repository.PusherRepository

class CheckPusherConnectionUseCase(private val pusherRepository: PusherRepository) {
    suspend operator fun invoke(): Result<Boolean> {
        return pusherRepository.checkConnection()
    }
}
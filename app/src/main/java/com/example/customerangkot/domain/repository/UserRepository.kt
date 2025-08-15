package com.example.customerangkot.domain.repository

import com.example.customerangkot.data.api.dto.GetProfileResponse
import com.example.customerangkot.data.api.dto.HistoryResponse
import com.example.customerangkot.data.api.dto.LogoutResponse
import com.example.customerangkot.data.api.dto.TopUpResponse
import com.example.customerangkot.domain.entity.Passenger
import com.example.customerangkot.domain.entity.User
import com.example.customerangkot.domain.entity.UserSession
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun login(email: String, password: String): Triple<User, Passenger, String>
    suspend fun saveSession(user: User, token: String, noHp: String, saldo: Int)
    fun getSession(): Flow<UserSession>
    suspend fun register(
        name: String,
        email: String,
        noHp: String,
        password: String
    ): Pair<User, Passenger>
    suspend fun logout(): LogoutResponse
    suspend fun topUp(nominal: Int): TopUpResponse
    suspend fun getSaldo() : TopUpResponse
    suspend fun getHistory(): HistoryResponse
    suspend fun getProfile() : GetProfileResponse
}
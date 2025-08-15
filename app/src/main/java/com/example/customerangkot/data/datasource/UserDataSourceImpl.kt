package com.example.customerangkot.data.datasource

import android.util.Log
import com.example.customerangkot.data.api.ApiService
import com.example.customerangkot.data.api.dto.GetProfileResponse
import com.example.customerangkot.data.api.dto.HistoryResponse
import com.example.customerangkot.data.api.dto.LoginSuccessResponse
import com.example.customerangkot.data.api.dto.LogoutResponse
import com.example.customerangkot.data.api.dto.RegisterSuccessResponse
import com.example.customerangkot.data.api.dto.TopUpResponse
import com.example.customerangkot.data.preference.UserPreference
import com.example.customerangkot.domain.entity.User
import com.example.customerangkot.domain.entity.UserSession
import kotlinx.coroutines.flow.Flow

class UserDataSourceImpl(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) : UserDataSource {

    private val TAG = "UserDataSourceImpl"

    override suspend fun register(
        name: String,
        email: String,
        noHp: String,
        password: String
    ): RegisterSuccessResponse {
        try {
            Log.d(TAG, "Calling register with name=$name, email=$email")
            return apiService.register(name, email, noHp, password)
        } catch (e: Exception) {
            Log.e(TAG, "Error in register: ${e.message}", e)
            throw e
        }
    }

    override suspend fun login(email: String, password: String): LoginSuccessResponse {
        try {
            Log.d(TAG, "Calling login with email=$email")
            val response = apiService.login(email, password)
            Log.d(TAG, "Login response: $response")
            return response
        } catch (e: Exception) {
            Log.e(TAG, "Error in login: ${e.message}", e)
            throw e
        }
    }

    override suspend fun saveSession(user: User, token: String, noHp: String, saldo: Int) {
        try {
            Log.d(TAG, "Saving session for user: ${user.name}")
            userPreference.saveSession(user, token, noHp, saldo)
        } catch (e: Exception) {
            Log.e(TAG, "Error in saveSession: ${e.message}", e)
            throw e
        }
    }

    override fun getSession(): Flow<UserSession> {
        return userPreference.getSession()
    }

    override suspend fun logout(): LogoutResponse {
        try {
            Log.d(TAG, "Calling logout")
            val response = apiService.logout("Bearer ${userPreference.getAuthToken()}")
            userPreference.logout()
            return response
        } catch (e: Exception) {
            Log.e(TAG, "Error in logout: ${e.message}", e)
            throw e
        }
    }

    override suspend fun topUp(nominal: Int): TopUpResponse {
        try {
            Log.d(TAG, "Calling topUp with nominal=$nominal")
            val token = userPreference.getAuthToken() ?: throw Exception("Token tidak ditemukan")
            return apiService.topUp("Bearer $token", nominal)
        } catch (e: Exception) {
            Log.e(TAG, "Error in topUp: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getSaldo(): TopUpResponse {
        try {
            Log.d(TAG, "Fetching saldo")
            val token = userPreference.getAuthToken() ?: throw Exception("Token tidak ditemukan")
            return apiService.getSaldo("Bearer $token")
        } catch (e: Exception) {
            Log.e(TAG, "Error in getSaldo: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getHistory(): HistoryResponse {
        try {
            Log.d(TAG, "Fetching history")
            val token = userPreference.getAuthToken() ?: throw Exception("Token tidak ditemukan")
            return apiService.getHistory("Bearer $token")
        } catch (e: Exception) {
            Log.e(TAG, "Error in getHistory: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getProfile(): GetProfileResponse {
        try {
            Log.d(TAG, "Fetching profile")
            val token = userPreference.getAuthToken() ?: throw Exception("Token tidak ditemukan")
            return apiService.getProfile("Bearer $token")
        } catch (e: Exception) {
            Log.e(TAG, "Error in getProfile: ${e.message}", e)
            throw e
        }
    }
}
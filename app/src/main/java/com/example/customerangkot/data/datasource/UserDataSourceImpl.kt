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
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

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
            val response = apiService.register(name, email, noHp, password)
            if (response.isSuccessful) {
                return response.body() ?: throw Exception("Respons kosong dari server")
            } else {
                // [Baru] Tangani error 422
                if (response.code() == 422) {
                    val errorBody = response.errorBody()?.string()
                    val errorJson = Gson().fromJson(errorBody, JsonObject::class.java)
                    val errorMessage = errorJson.get("message")?.asString ?: "Validasi gagal"
                    val errors = errorJson.getAsJsonObject("errors")
                    val detailedError = errors?.entrySet()?.joinToString(", ") { entry ->
                        entry.value.asJsonArray.joinToString(", ") { it.asString }
                    } ?: errorMessage
                    throw Exception(detailedError)
                }
                throw HttpException(response)
            }
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
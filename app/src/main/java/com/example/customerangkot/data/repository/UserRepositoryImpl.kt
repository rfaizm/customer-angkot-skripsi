package com.example.customerangkot.data.repository

import android.util.Log
import com.example.customerangkot.data.api.ApiService
import com.example.customerangkot.data.api.dto.GetProfileResponse
import com.example.customerangkot.data.api.dto.HistoryResponse
import com.example.customerangkot.data.api.dto.LogoutResponse
import com.example.customerangkot.data.api.dto.TopUpResponse
import com.example.customerangkot.data.datasource.UserDataSource
import com.example.customerangkot.data.preference.UserPreference
import com.example.customerangkot.domain.entity.Passenger
import com.example.customerangkot.domain.entity.User
import com.example.customerangkot.domain.entity.UserSession
import com.example.customerangkot.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

class UserRepositoryImpl(
    private val userDataSource: UserDataSource
) : UserRepository {

    private val TAG = "UserRepositoryImpl"

    override suspend fun register(
        name: String,
        email: String,
        noHp: String,
        password: String
    ): Pair<User, Passenger> {
        try {
            Log.d(TAG, "Calling register with name=$name, email=$email")
            val response = userDataSource.register(name, email, noHp, password)
            val userJson = response.user ?: throw Exception("User tidak ditemukan")
            val passengerJson = response.passenger ?: throw Exception("Passenger tidak ditemukan")
            val user = User(
                id = userJson.id ?: throw Exception("ID pengguna tidak ditemukan"),
                email = userJson.email ?: throw Exception("Email tidak ditemukan"),
                name = userJson.name ?: throw Exception("Nama tidak ditemukan"),
                role = userJson.role ?: throw Exception("Role tidak ditemukan")
            )
            val passenger = Passenger(
                id = passengerJson.id ?: throw Exception("ID penumpang tidak ditemukan"),
                fullName = passengerJson.fullName ?: throw Exception("Nama lengkap tidak ditemukan"),
                noHp = passengerJson.noHp ?: throw Exception("Nomor HP tidak ditemukan"),
                saldo = passengerJson.saldo ?: throw Exception("Saldo tidak ditemukan")
            )
            return Pair(user, passenger)
        } catch (e: Exception) {
            Log.e(TAG, "Error in register: ${e.message}", e)
            throw e
        }
    }

    override suspend fun login(email: String, password: String): Triple<User, Passenger, String> {
        try {
            Log.d(TAG, "Calling login with email=$email")
            val response = userDataSource.login(email, password)
            Log.d(TAG, "Login response: $response")
            val userLoginJson = response.user ?: throw Exception("User tidak ditemukan")
            val passengerLoginJson = userLoginJson.passenger ?: throw Exception("Passenger tidak ditemukan")
            val token = response.token ?: throw Exception("Token tidak ditemukan")
            val user = User(
                id = userLoginJson.id ?: throw Exception("ID pengguna tidak ditemukan"),
                email = userLoginJson.email ?: throw Exception("Email tidak ditemukan"),
                name = userLoginJson.name ?: throw Exception("Nama tidak ditemukan"),
                role = userLoginJson.role ?: throw Exception("Role tidak ditemukan")
            )
            val passenger = Passenger(
                id = passengerLoginJson.id ?: throw Exception("ID penumpang tidak ditemukan"),
                fullName = passengerLoginJson.fullName ?: throw Exception("Nama lengkap tidak ditemukan"),
                noHp = passengerLoginJson.noHp ?: throw Exception("Nomor HP tidak ditemukan"),
                saldo = passengerLoginJson.saldo ?: throw Exception("Saldo tidak ditemukan")
            )
            return Triple(user, passenger, token)
        } catch (e: Exception) {
            Log.e(TAG, "Error in login: ${e.message}", e)
            throw e
        }
    }

    override suspend fun saveSession(user: User, token: String, noHp: String, saldo: Int) {
        try {
            Log.d(TAG, "Saving session for user: ${user.name}")
            userDataSource.saveSession(user, token, noHp, saldo)
        } catch (e: Exception) {
            Log.e(TAG, "Error in saveSession: ${e.message}", e)
            throw e
        }
    }

    override fun getSession(): Flow<UserSession> {
        return userDataSource.getSession()
    }

    override suspend fun logout(): LogoutResponse {
        try {
            Log.d(TAG, "Calling logout")
            val response = userDataSource.logout()
            return response
        } catch (e: Exception) {
            Log.e(TAG, "Error in logout: ${e.message}", e)
            throw e
        }
    }

    override suspend fun topUp(nominal: Int): TopUpResponse {
        try {
            Log.d(TAG, "Calling topUp with nominal=$nominal")
            return userDataSource.topUp(nominal)
        } catch (e: Exception) {
            Log.e(TAG, "Error in topUp: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getSaldo(): TopUpResponse {
        try {
            Log.d(TAG, "Fetching saldo")
            return userDataSource.getSaldo()
        } catch (e: Exception) {
            Log.e(TAG, "Error in getSaldo: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getHistory(): HistoryResponse {
        try {
            Log.d(TAG, "Fetching history")
            return userDataSource.getHistory()
        } catch (e: Exception) {
            Log.e(TAG, "Error in getHistory: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getProfile(): GetProfileResponse {
        try {
            Log.d(TAG, "Fetching profile")
            return userDataSource.getProfile()
        } catch (e: Exception) {
            Log.e(TAG, "Error in getProfile: ${e.message}", e)
            throw e
        }
    }
}
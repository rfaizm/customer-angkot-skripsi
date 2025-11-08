package com.example.customerangkot.data.datasource

import android.util.Log
import com.example.customerangkot.data.api.ApiService
import com.example.customerangkot.data.api.dto.FindClosestResponse
import com.example.customerangkot.data.api.dto.GetDriverResponse
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.HttpException

class TrayekDataSourceImpl(
    private val apiService: ApiService
) : TrayekDataSource {

    private val TAG = "TrayekDataSourceImpl"

    override suspend fun getClosestTrayek(token: String, lat: Double, lng: Double): FindClosestResponse {
        try {
            Log.d(TAG, "Fetching closest trayek with lat=$lat, lng=$lng")
            return apiService.trayekClosest("Bearer $token", lat, lng)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching closest trayek: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getAllAngkotByIdTrayek(token: String, lat: Double, lng: Double, trayekId: Int): FindClosestResponse {
        try {
            Log.d(TAG, "Fetching angkot by trayekId=$trayekId with lat=$lat, lng=$lng")
            return apiService.showAngkotLocationByIdTrayek("Bearer $token", trayekId, lat, lng)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching angkot by trayekId: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getIdDriver(
        token: String,
        angkotId: Int
    ): GetDriverResponse {
        try {
            Log.d(TAG, "Fetching driver by angkot id = $angkotId")
            val response = apiService.getDriver("Bearer $token", angkotId)

            if (response.isSuccessful) {
                Log.d(TAG, "getDriver response: ${response.body()}")
                return response.body() ?: throw Exception("Respons kosong dari server")
            } else {
                throw HttpException(response)
            }
        } catch (e: Exception) {
            val response = apiService.getDriver("Bearer $token", angkotId)
            if (!response.isSuccessful){
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Error response body from getDriver: $errorBody")
                val errorJson = Gson().fromJson(errorBody, JsonObject::class.java)
                val errorMessage = errorJson.get("message")?.asString ?: "Terdapat Kesalahan Pada Server"
                throw Exception(errorMessage)
            }
            Log.e(TAG, "Error in get driver id: ${e.message}", e)
            throw e
        }
    }
}
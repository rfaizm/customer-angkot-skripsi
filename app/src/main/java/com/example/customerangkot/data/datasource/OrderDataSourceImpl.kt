package com.example.customerangkot.data.datasource

import android.util.Log
import com.example.customerangkot.data.api.ApiService
import com.example.customerangkot.data.api.dto.CheckOrderActiveResponse
import com.example.customerangkot.data.api.dto.GetETAResponse
import com.example.customerangkot.data.api.dto.OrderCancelResponse
import com.example.customerangkot.data.api.dto.OrderCreatedResponse
import com.example.customerangkot.data.preference.UserPreference
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response

class OrderDataSourceImpl(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) : OrderDataSource {

    private val TAG = "OrderDataSourceImpl"

    override suspend fun createOrder(
        token: String,
        driverId: Int,
        startLat: Double,
        startLong: Double,
        destinationLat: Double,
        destinationLong: Double,
        numberOfPassengers: Int,
        totalPrice: Double,
        methodPayment: String,
        polyline: String
    ): OrderCreatedResponse {
        return try {
            Log.d(TAG, "Creating order: driverId=$driverId, totalPrice=$totalPrice, method=$methodPayment")
            val response: Response<OrderCreatedResponse> = apiService.createOrder(
                "Bearer $token",
                driverId,
                startLat,
                startLong,
                destinationLat,
                destinationLong,
                numberOfPassengers,
                totalPrice.toInt(),
                methodPayment,
                polyline
            )
            Log.d(TAG, "Create order response: $polyline")

            if (response.isSuccessful) {
                response.body() ?: throw Exception("Respons kosong dari server")
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                Log.e(TAG, "Create order failed: HTTP ${response.code()}, Body: $errorBody")

                val errorMessage = try {
                    val json = JSONObject(errorBody)
                    when (response.code()) {
                        400, 403, 404 -> {
                            json.optString("message", "Gagal membuat pesanan")
                        }
                        422 -> {
                            val errors = json.optJSONObject("errors")
                            val firstError = errors?.keys()?.next()?.let { key ->
                                errors.getJSONArray(key).getString(0)
                            }
                            firstError ?: json.optString("message", "Validasi gagal")
                        }
                        500 -> {
                            json.optString("message", "Server error")
                        }
                        else -> "Error ${response.code()}: ${response.message()}"
                    }
                } catch (e: Exception) {
                    "Gagal membuat pesanan"
                }

                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in createOrder: ${e.message}", e)
            throw e
        }
    }

    override suspend fun cancelOrder(token: String, orderId: Int): OrderCancelResponse {
        try {
            Log.d(TAG, "Canceling order with orderId=$orderId")
            return apiService.cancelOrder("Bearer $token", orderId)
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling order: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getETA(
        token: String,
        startLat: Double,
        startLong: Double,
        endLat: Double,
        endLong: Double
    ): GetETAResponse {
        try {
            Log.d(TAG, "Fetching ETA with startLat=$startLat, startLong=$startLong, endLat=$endLat, endLong=$endLong")
            return apiService.getEta("Bearer $token", startLat, startLong, endLat, endLong)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching ETA: ${e.message}", e)
            throw e
        }
    }

    override suspend fun getCheckOrderActive(token: String): CheckOrderActiveResponse {
        try {
            val response = apiService.getCheckActiveOrder("Bearer $token")

            if (response.isSuccessful) {
                Log.d(TAG, "getCheckOrderActive response: ${response.body()}")
                return response.body() ?: throw Exception("Respons kosong dari server")
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                Log.d(TAG, "Error response body: $errorBody")
                throw HttpException(response)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching ETA: ${e.message}", e)
            throw e
        }
    }
}
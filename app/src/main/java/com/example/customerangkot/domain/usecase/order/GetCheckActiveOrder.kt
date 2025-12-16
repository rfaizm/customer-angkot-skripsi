package com.example.customerangkot.domain.usecase.order

import android.util.Log
import com.example.customerangkot.data.api.dto.CheckOrderActiveResponse
import com.example.customerangkot.data.preference.UserPreference
import com.example.customerangkot.domain.repository.OrderRepository

//class GetCheckActiveOrder(
//    private val orderRepository: OrderRepository,
//    private val userPreference: UserPreference
//) {
//    suspend operator fun invoke(
//
//    ) : Result<CheckOrderActiveResponse> {
//        return try{
//            val token = userPreference.getAuthToken()
//            if (token == null) {
//                Log.e("GetDriverIdWithAngkotId", "Token tidak ditemukan")
//                return Result.failure(Exception("Token tidak valid"))
//            }
//            Log.d("GetCheckActiveOrder", "Menggunakan token: $token")
//
//            val response = orderRepository.getCheckOrderActive(token)
//            Result.success(response)
//        } catch (e: Exception) {
//            // [FIX] Kirim pesan asli dari backend
//            Result.failure(Exception("Gagal Untuk GetCheckActiveOrder: ${e.message}"))
//        }
//    }
//}
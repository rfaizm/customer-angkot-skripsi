package com.example.customerangkot.domain.usecase.trayek

import com.example.customerangkot.data.api.dto.DataTrayekJSON
import com.example.customerangkot.data.preference.UserPreference
import com.example.customerangkot.domain.entity.TrayekItem
import com.example.customerangkot.domain.repository.TrayekRepository

class GetClosestTrayekUseCase(
    private val trayekRepository: TrayekRepository,
    private val userPreference: UserPreference
) {
    suspend fun getUniqueTrayeks(lat: Double, lng: Double): Result<List<TrayekItem>> {
        return try {
            val token = userPreference.getAuthToken()
                ?: return Result.failure(Exception("Token tidak ditemukan"))
            val response = trayekRepository.getClosestTrayek(token, lat, lng)
            if (response.status == "success" && response.data != null) {
                val trayekMap = mutableMapOf<Int, MutableList<DataTrayekJSON>>()
                response.data.filterNotNull().forEach { item ->
                    val trayek = item.trayek
                    if (trayek != null && trayek.id != null) {
                        val trayekId = trayek.id
                        trayekMap.getOrPut(trayekId) { mutableListOf() }.add(item)
                    }
                }
                val trayekItems = trayekMap.map { (id, items) ->
                    TrayekItem(
                        trayekId = id,
                        name = items.first().trayek?.name ?: "Unknown",
                        description = items.first().trayek?.description,
                        imageUrl = items.first().trayek?.imageUrl,
                        angkotIds = items.mapNotNull { it.angkotId },
                        latitudes = items.mapNotNull { it.lat?.toDoubleOrNull() },
                        longitudes = items.mapNotNull { it.long?.toDoubleOrNull() }
                    )
                }
                Result.success(trayekItems)
            } else {
                Result.failure(Exception(response.message ?: "Gagal mengambil trayek terdekat"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllTrayeks(lat: Double, lng: Double): Result<List<DataTrayekJSON>> {
        return try {
            val token = userPreference.getAuthToken()
                ?: return Result.failure(Exception("Token tidak ditemukan"))
            val response = trayekRepository.getClosestTrayek(token, lat, lng)
            if (response.status == "success" && response.data != null) {
                Result.success(response.data.filterNotNull())
            } else {
                Result.failure(Exception(response.message ?: "Gagal mengambil trayek terdekat"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
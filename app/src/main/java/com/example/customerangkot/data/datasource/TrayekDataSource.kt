package com.example.customerangkot.data.datasource

import com.example.customerangkot.data.api.dto.FindClosestResponse

interface TrayekDataSource {
    suspend fun getClosestTrayek(token: String, lat: Double, lng: Double): FindClosestResponse
    suspend fun getAllAngkotByIdTrayek(token: String, lat: Double, lng: Double, trayekId: Int): FindClosestResponse
}
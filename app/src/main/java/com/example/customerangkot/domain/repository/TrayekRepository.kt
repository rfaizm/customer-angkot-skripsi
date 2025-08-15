package com.example.customerangkot.domain.repository

import com.example.customerangkot.data.api.dto.FindClosestResponse

interface TrayekRepository {
    suspend fun getClosestTrayek(token: String, lat: Double, lng: Double): FindClosestResponse

    suspend fun getAllAngkotByIdTrayek(token : String, lat : Double, lng : Double, trayekId : Int) : FindClosestResponse
}
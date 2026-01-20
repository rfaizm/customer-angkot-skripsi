package com.example.customerangkot.domain.repository

import com.example.customerangkot.data.api.dto.AngkotFilterResponse
import com.example.customerangkot.data.api.dto.FindClosestResponse
import com.example.customerangkot.data.api.dto.GetDriverResponse

interface TrayekRepository {
    suspend fun getClosestTrayek(token: String, lat: Double, lng: Double): FindClosestResponse

    suspend fun getAllAngkotByIdTrayek(token : String, lat : Double, lng : Double, trayekId : Int) : FindClosestResponse

    suspend fun getIdDriverWithAngkotId(token : String, angkotId : Int) : GetDriverResponse

    suspend fun getAngkotFilterByTrayek(token: String, trayekId: Int, lat: Double, lng: Double, polyline: String): AngkotFilterResponse
}
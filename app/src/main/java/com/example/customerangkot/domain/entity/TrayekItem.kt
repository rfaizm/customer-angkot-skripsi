package com.example.customerangkot.domain.entity

import android.content.Context
import com.example.customerangkot.R

data class TrayekItem(
    val trayekId: Int,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val angkotIds: List<Int>,
    val latitudes: List<Double>, // Baris 9: Daftar latitude
    val longitudes: List<Double> // Baris 10: Daftar longitude
)



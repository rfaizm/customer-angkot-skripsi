package com.example.customerangkot.domain.entity

import android.content.Context
import com.example.customerangkot.R

data class TrayekItem(
    val trayekId: Int,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val angkotIds: List<Int>,
    val latitudes: List<Double>,
    val longitudes: List<Double>,
    val platNomors: List<String> = emptyList()  // [BARU] Tambahkan ini
)



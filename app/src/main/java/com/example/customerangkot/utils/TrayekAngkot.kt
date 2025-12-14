package com.example.customerangkot.utils

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class InformationTrayek(
    val namaTrayek: String,
    val description: String,
    val imageUrl: String
) : Parcelable

data class DataHistory(
    val namaTrayek: String,
    val numberPlat: String,
    val driverName: String,
    val date: String,
    val price: String
)

data class RouteAngkot(
    val trayekId: Int,
    val namaTrayek: String,
    val predictETA: String,
    val price: Double,
    val polyline : String,
    val startLat : Double,
    val startLong : Double,
    val destinationLat : Double,
    val destinationLong : Double,
    val isIntegrated: Boolean = trayekId > 0,
    val color : String
)

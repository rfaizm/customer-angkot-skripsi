package com.example.customerangkot.domain.entity

sealed class InformationItem {
    data class TrayekInformation(
        val trayekId: Int,
        val name: String,
        val description: String?,
        val imageUrl: String?,
    ) : InformationItem()

    data class AngkotInformation(
        val angkotId: Int,
        val platNomor: String,
        val distanceKm: Double,
        val trayekId: Int,
        val imageUrl: String?, // Baris 15: Tambahkan imageUrl
        val latitude: Double,
        val longtitude: Double
    ) : InformationItem()
}
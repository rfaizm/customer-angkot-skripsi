package com.example.customerangkot.domain.entity

data class FilterAngkotRequest(
    val trayekId: Int,
    val lat: Double,
    val long: Double,
    val polyline: String
)
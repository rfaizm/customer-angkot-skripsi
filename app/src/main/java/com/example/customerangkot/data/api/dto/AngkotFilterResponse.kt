package com.example.customerangkot.data.api.dto

import com.google.gson.annotations.SerializedName

data class AngkotFilterResponse(

	@field:SerializedName("data")
	val data: List<DataAngkotFilter?>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class DataAngkotFilter(

	@field:SerializedName("distanceKm")
	var distanceKm: Any? = null,

	@field:SerializedName("maxCapacity")
	val maxCapacity: String? = null,

	@field:SerializedName("trayek")
	val trayek: TrayekFilter? = null,

	@field:SerializedName("angkotId")
	val angkotId: Int? = null,

	@field:SerializedName("currentPassengers")
	val currentPassengers: Int? = null,

	@field:SerializedName("platNomor")
	val platNomor: String? = null,

	@field:SerializedName("lat")
	val lat: String? = null,

	@field:SerializedName("long")
	val long: String? = null
)

data class TrayekFilter(

	@field:SerializedName("imageUrl")
	val imageUrl: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

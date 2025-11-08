package com.example.customerangkot.data.api.dto

import com.google.gson.annotations.SerializedName

data class FindClosestResponse(

	@field:SerializedName("data")
	val data: List<DataTrayekJSON?>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class TrayekJSON(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("imageUrl")
	val imageUrl: String? = null
)

data class DataTrayekJSON(
	@field:SerializedName("driver_id")
	val driverId: Int? = null,

	@field:SerializedName("distanceKm")
	val distanceKm: Any? = null,

	@field:SerializedName("trayek")
	val trayek: TrayekJSON? = null,

	@field:SerializedName("platNomor")
	val platNomor: String? = null,

	@field:SerializedName("angkotId")
	val angkotId: Int? = null,

	@field:SerializedName("lat")
	val lat: String? = null,

	@field:SerializedName("long")
	val long: String? = null
)

package com.example.customerangkot.data.api.dto

import com.google.gson.annotations.SerializedName

data class PlaceToCoordinateResponse(

	@field:SerializedName("data")
	val data: List<PlaceName?>? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class PlaceName(

	@field:SerializedName("placeName")
	val placeName: String? = null,

	@field:SerializedName("types")
	val types: List<String?>? = null,

	@field:SerializedName("distanceKm")
	val distanceKm: Double? = null,

	@field:SerializedName("lat")
	val lat: Double? = null,

	@field:SerializedName("long")
	val long: Double? = null,

	@field:SerializedName("placeId")
	val placeId: String? = null
)

package com.example.customerangkot.data.api.dto

import com.google.gson.annotations.SerializedName

data class PlaceNameResponse(

	@field:SerializedName("data")
	val data: PlaceNameDataJSON? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("message")
	val message : String? = null
)

data class PlaceNameDataJSON(

	@field:SerializedName("placeName")
	val placeName: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("lat")
	val lat: Double? = null,

	@field:SerializedName("long")
	val long: Double? = null
)

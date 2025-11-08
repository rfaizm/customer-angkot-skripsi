package com.example.customerangkot.data.api.dto

import com.google.gson.annotations.SerializedName

data class GetDriverResponse(

	@field:SerializedName("data")
	val data: DataIdDriver? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class DataIdDriver(

	@field:SerializedName("driver_id")
	val driverId: Int? = null
)

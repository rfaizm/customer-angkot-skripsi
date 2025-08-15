package com.example.customerangkot.data.api.dto

import com.google.gson.annotations.SerializedName

data class GetETAResponse(

	@field:SerializedName("data")
	val data: DataETA? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class DataETA(

	@field:SerializedName("eta")
	val eta: String? = null,

	@field:SerializedName("eta_seconds")
	val etaSeconds: Int? = null
)

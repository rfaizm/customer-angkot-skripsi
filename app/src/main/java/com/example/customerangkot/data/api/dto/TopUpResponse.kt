package com.example.customerangkot.data.api.dto

import com.google.gson.annotations.SerializedName

data class TopUpResponse(

	@field:SerializedName("data")
	val data: Wallet? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class Wallet(

	@field:SerializedName("saldo")
	val saldo: Int? = null
)

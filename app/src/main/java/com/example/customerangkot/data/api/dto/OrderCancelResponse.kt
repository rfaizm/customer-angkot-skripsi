package com.example.customerangkot.data.api.dto

import com.google.gson.annotations.SerializedName

data class OrderCancelResponse(

	@field:SerializedName("data")
	val data: CancelOrderJSON? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class CancelOrderJSON(

	@field:SerializedName("returned_amount")
	val returnedAmount: Int? = null,

	@field:SerializedName("order_id")
	val orderId: Int? = null,

	@field:SerializedName("status")
	val status: String? = null
)

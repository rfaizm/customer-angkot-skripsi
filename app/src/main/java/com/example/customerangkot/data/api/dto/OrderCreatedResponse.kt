package com.example.customerangkot.data.api.dto

import com.google.gson.annotations.SerializedName

data class OrderCreatedResponse(

	@field:SerializedName("data")
	val data: OrderJSON? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class OrderJSON(

	@field:SerializedName("driver_id")
	val driverId: Int? = null,

	@field:SerializedName("number_of_passengers")
	val numberOfPassengers: Int? = null,

	@field:SerializedName("price")
	val price: Int? = null,

	@field:SerializedName("driver_full_name")
	val fullName : String? = null,

	@field:SerializedName("angkot_plat_nomor")
	val platNomor : String? = null,

	@field:SerializedName("angkot_lat")
	val lat : Double? = null,

	@field:SerializedName("angkot_long")
	val long : Double? = null,

	@field:SerializedName("order_id")
	val orderId: Int? = null,

	@field:SerializedName("angkot_id")
	val angkotId: Int? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("payment_method")
	val methodPayment: String? = null
)

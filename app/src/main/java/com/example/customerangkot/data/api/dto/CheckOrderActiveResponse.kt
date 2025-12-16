package com.example.customerangkot.data.api.dto

import com.google.gson.annotations.SerializedName

data class CheckOrderActiveResponse(

	@field:SerializedName("data")
	val data: DataCheckOrderActive? = null,

	@field:SerializedName("has_active_order")
	val hasActiveOrder: Boolean? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class DataCheckOrderActive(

	@field:SerializedName("startLat")
	val startLat: Any? = null,

	@field:SerializedName("statusOrder")
	val statusOrder: String? = null,

	@field:SerializedName("orderId")
	val orderId: Int? = null,

	@field:SerializedName("totalPrice")
	val totalPrice: Int? = null,

	@field:SerializedName("destinationLong")
	val destinationLong: Any? = null,

	@field:SerializedName("methodPayment")
	val methodPayment: String? = null,

	@field:SerializedName("timeOrder")
	val timeOrder: String? = null,

	@field:SerializedName("startLong")
	val startLong: Any? = null,

	@field:SerializedName("angkot")
	val angkot: AngkotData? = null,

	@field:SerializedName("driver")
	val driver: DriverData? = null,

	@field:SerializedName("numberOfPassengers")
	val numberOfPassengers: Int? = null,

	@field:SerializedName("destinationLat")
	val destinationLat: Any? = null,

	@field:SerializedName("polyline")
	val polyline: String? = null
)

data class AngkotData(

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("platNomor")
	val platNomor: String? = null
)

data class DriverData(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

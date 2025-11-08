package com.example.customerangkot.data.api.dto

import com.google.gson.annotations.SerializedName

data class HistoryResponse(

	@field:SerializedName("data")
	val data: List<DataHistoryItem?>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class DataHistoryItem(

	@field:SerializedName("driver_name")
	val driverName: String? = null,

	@field:SerializedName("order_date")
	val orderDate: String? = null,

	@field:SerializedName("vehicle_plate")
	val vehiclePlate: String? = null,

	@field:SerializedName("total_price")
	val totalPrice: Int? = null,

	@field:SerializedName("trayek")
	val trayek: String? = null,

	@field:SerializedName("payment_method")
	val paymentMethod: String? = null
)

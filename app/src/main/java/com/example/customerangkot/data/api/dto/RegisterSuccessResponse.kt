package com.example.customerangkot.data.api.dto

import com.google.gson.annotations.SerializedName

data class RegisterSuccessResponse(

	@field:SerializedName("passenger")
	val passenger: PassengerJSON? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("user")
	val user: UserJSON? = null
)

data class PassengerJSON(

	@field:SerializedName("full_name")
	val fullName: String? = null,

	@field:SerializedName("no_hp")
	val noHp: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("saldo")
	val saldo: Int? = null
)

data class UserJSON(

	@field:SerializedName("role")
	val role: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("email")
	val email: String? = null
)

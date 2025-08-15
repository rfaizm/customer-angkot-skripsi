package com.example.customerangkot.data.api.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class LoginSuccessResponse(

	@SerialName("message")
	val message: String? = null,

	@SerialName("user")
	val user: UserLoginJSON? = null,

	@SerialName("token")
	val token: String? = null
)

@Serializable
data class UserLoginJSON(

	@SerialName("role")
	val role: String? = null,

	@SerialName("passenger")
	val passenger: PassengerLoginJSON? = null,

	@SerialName("name")
	val name: String? = null,

	@SerialName("id")
	val id: Int? = null,

	@SerialName("email")
	val email: String? = null
)

@Serializable
data class PassengerLoginJSON(
	@SerialName("id")
	val id: Int? = null,

	@SerialName("fullName")
	val fullName: String? = null,

	@SerialName("noHp")
	val noHp: String? = null,

	@SerialName("saldo")
	val saldo: Int? = null
)

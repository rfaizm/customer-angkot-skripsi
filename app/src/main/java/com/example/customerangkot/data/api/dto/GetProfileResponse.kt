package com.example.customerangkot.data.api.dto

import com.google.gson.annotations.SerializedName

data class GetProfileResponse(

	@field:SerializedName("data")
	val data: ProfileDataJSON? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class ProfileDataJSON(

	@field:SerializedName("full_name")
	val fullName: String? = null,

	@field:SerializedName("no_hp")
	val noHp: String? = null,

	@field:SerializedName("email")
	val email: String? = null
)

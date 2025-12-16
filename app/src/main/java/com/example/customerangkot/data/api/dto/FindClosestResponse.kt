package com.example.customerangkot.data.api.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class FindClosestResponse(

	@field:SerializedName("data")
	val data: List<DataTrayekJSON?>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

@Parcelize
data class TrayekJSON(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("imageUrl")
	val imageUrl: String? = null
) : Parcelable


@Parcelize
data class DataTrayekJSON(

	@field:SerializedName("distanceKm")
	var distanceKm: Double? = null,

	@field:SerializedName("trayek")
	val trayek: TrayekJSON? = null,

	@field:SerializedName("platNomor")
	val platNomor: String? = null,

	@field:SerializedName("angkotId")
	val angkotId: Int? = null,

	@field:SerializedName("lat")
	val lat: String? = null,

	@field:SerializedName("long")
	val long: String? = null
) : Parcelable

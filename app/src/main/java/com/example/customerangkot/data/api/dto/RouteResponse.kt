package com.example.customerangkot.data.api.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class RouteResponse(

	@field:SerializedName("data")
	val data: List<DataItem?>? = null,

	@field:SerializedName("status")
	val status: String? = null
)

@Parcelize
data class StepsItem(

	@field:SerializedName("duration")
	val duration: String? = null,

	@field:SerializedName("distance")
	val distance: String? = null,

	@field:SerializedName("travel_mode")
	val travelMode: String? = null,

	@field:SerializedName("polyline")
	val polyline: String? = null,

	@field:SerializedName("transit_details")
	val transitDetails: TransitDetails? = null
) : Parcelable

@Parcelize
data class ArrivalLocation(

	@field:SerializedName("lng")
	val lng: Double? = null,

	@field:SerializedName("lat")
	val lat: Double? = null
) : Parcelable

@Parcelize
data class DepartureLocation(

	@field:SerializedName("lng")
	val lng: Double? = null,

	@field:SerializedName("lat")
	val lat: Double? = null
) : Parcelable

data class TrayeksItem(

	@field:SerializedName("duration")
	val duration: String? = null,

	@field:SerializedName("color")
	val color: String? = null,

	@field:SerializedName("price")
	val price: Int? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("arrival_stop")
	val arrival : String? = null,

	@field:SerializedName("departure_stop")
	val departure : String? = null,

	@field:SerializedName("polyline")
	val polyline : String? = null,

	@field:SerializedName("start_lat")
	val startLat : Double? = null,

	@field:SerializedName("start_long")
	val startLong : Double? = null,

	@field:SerializedName("destination_lat")
	val destinationLat : Double? = null,

	@field:SerializedName("destination_long")
	val destinationLong : Double? = null,
)

data class DataItem(

	@field:SerializedName("duration")
	val duration: String? = null,

	@field:SerializedName("start_location")
	val startLocation: StartLocation? = null,

	@field:SerializedName("distance")
	val distance: String? = null,

	@field:SerializedName("end_location")
	val endLocation: EndLocation? = null,

	@field:SerializedName("steps")
	val steps: List<StepsItem?>? = null,

	@field:SerializedName("trayeks")
	val trayeks: List<TrayeksItem?>? = null
)

data class EndLocation(

	@field:SerializedName("lat")
	val lat: Double? = null,

	@field:SerializedName("long")
	val long: Double? = null
)

@Parcelize
data class TransitDetails(

	@field:SerializedName("color")
	val color: String? = null,

	@field:SerializedName("trayek_name")
	val trayekName: String? = null,

	@field:SerializedName("price")
	val price: Int? = null,

	@field:SerializedName("arrival_stop")
	val arrivalStop: String? = null,

	@field:SerializedName("departure_stop")
	val departureStop: String? = null,

	@field:SerializedName("line_name")
	val lineName: String? = null,

	@field:SerializedName("arrival_location")
	val arrivalLocation: ArrivalLocation? = null,

	@field:SerializedName("departure_location")
	val departureLocation: DepartureLocation? = null,

	@field:SerializedName("trayek_id")
	val trayekId: Int? = null
) : Parcelable

data class StartLocation(

	@field:SerializedName("lat")
	val lat: Double? = null,

	@field:SerializedName("long")
	val jsonMemberLong: Double? = null
)

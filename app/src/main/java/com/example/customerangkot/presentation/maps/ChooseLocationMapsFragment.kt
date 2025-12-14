package com.example.customerangkot.presentation.maps

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.customerangkot.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ChooseLocationMapsFragment : Fragment() {

    private val callback = OnMapReadyCallback { googleMap ->
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isIndoorLevelPickerEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true
        // Inisialisasi peta dengan lokasi default, yaitu Bandung
        val lat = arguments?.getDouble(ARG_INITIAL_LAT, Double.NaN)
        val lng = arguments?.getDouble(ARG_INITIAL_LONG, Double.NaN)

        val initialLocation = if (lat != null && lng != null && !lat.isNaN() && !lng.isNaN()) {
            LatLng(lat, lng) // Lokasi terkini user
        } else {
            LatLng(-6.9175, 107.6191) // fallback (Bandung)
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 15f))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_choose_location_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        // Ambil locationType dari arguments
        val locationType = arguments?.getString(ARG_LOCATION_TYPE) ?: "start"

        // Tombol konfirmasi
        val fabConfirm = view.findViewById<FloatingActionButton>(R.id.fab_confirm_location)
        fabConfirm.setOnClickListener {
            mapFragment?.getMapAsync { googleMap ->
                val centerLatLng = googleMap.cameraPosition.target
                val latitude = centerLatLng.latitude
                val longitude = centerLatLng.longitude

                // Kembalikan hasil ke MapRouteFragment
                val result = Bundle().apply {
                    putDouble(KEY_LATITUDE, latitude)
                    putDouble(KEY_LONGITUDE, longitude)
                }
                parentFragmentManager.setFragmentResult("$REQUEST_KEY:$locationType", result)

                // Kembali ke MapRouteFragment
                parentFragmentManager.popBackStack()
            }
        }
    }

    companion object {
        private const val ARG_LOCATION_TYPE = "location_type"
        private const val ARG_INITIAL_LAT = "initial_lat"
        private const val ARG_INITIAL_LONG = "initial_long"
        private const val REQUEST_KEY = "location_result"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"

        fun newInstance(locationType: String, initialLat: Double?,
                        initialLong: Double?): ChooseLocationMapsFragment {
            return ChooseLocationMapsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_LOCATION_TYPE, locationType)
                    initialLat?.let { putDouble(ARG_INITIAL_LAT, it) }
                    initialLong?.let { putDouble(ARG_INITIAL_LONG, it) }
                }
            }
        }
    }
}
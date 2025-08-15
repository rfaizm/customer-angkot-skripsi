package com.example.customerangkot.presentation.maps

import android.content.pm.PackageManager
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.customerangkot.R
import com.example.customerangkot.data.api.dto.StepsItem
import com.example.customerangkot.utils.LocationPermissionListener
import com.example.customerangkot.utils.OnMarkerClickListener

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil

class MapsFragment : Fragment() {

    private lateinit var mMap: GoogleMap
    private var permissionListener: LocationPermissionListener? = null
    private var markerClickListener: OnMarkerClickListener? = null
    private var isMapReady = false
    private val angkotMarkers = mutableMapOf<Int, Marker>()
    private val polylines = mutableListOf<Polyline>()

    // Tambahkan LiveData untuk status peta
    private val _mapReadyLiveData = MutableLiveData<Boolean>()
    val mapReadyLiveData: LiveData<Boolean> get() = _mapReadyLiveData

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                if (isAdded) {
                    getMyLocation()
                    permissionListener?.onLocationPermissionGranted()
                }
            }
        }

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        isMapReady = true
        _mapReadyLiveData.value = true // Beri tahu bahwa peta siap

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isIndoorLevelPickerEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true

        // Tambahkan listener klik marker
        mMap.setOnMarkerClickListener { marker ->
            if (isAdded) {
                val angkotId = angkotMarkers.entries.find { it.value == marker }?.key
                if (angkotId != null) {
                    markerClickListener?.onMarkerClicked(angkotId)
                    marker.showInfoWindow() // Tampilkan info window secara eksplisit
                    Log.d("MapsFragment", "Marker clicked: Angkot ID $angkotId")
                } else {
                    Log.d("MapsFragment", "Marker clicked: No matching angkot ID found")
                }
                true // Konsumsi event klik
            } else {
                Log.d("MapsFragment", "Marker click skipped: Fragment not attached")
                false
            }
        }

        viewLifecycleOwnerLiveData.observe(viewLifecycleOwner) { owner ->
            if (owner != null && isAdded) {
                getMyLocation()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        if (parentFragment is LocationPermissionListener) {
            permissionListener = parentFragment as LocationPermissionListener
        }
        if (parentFragment is OnMarkerClickListener) {
            markerClickListener = parentFragment as OnMarkerClickListener
        }
    }

    private fun getMyLocation() {
        if (!isAdded || context == null) {
            Log.d("MapsFragment", "getMyLocation skipped: Fragment not attached")
            return
        }
        if (!isMapReady) {
            Log.d("MapsFragment", "getMyLocation skipped: Map not ready")
            return
        }
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            permissionListener?.onLocationPermissionGranted()
            Log.d("MapsFragment", "Lokasi diaktifkan")
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            Log.d("MapsFragment", "Meminta izin lokasi")
        }
    }

    fun animateCameraToLocation(lat: Double, lng: Double) {
        if (!isAdded || !isMapReady) {
            Log.d("MapsFragment", "animateCameraToLocation skipped: Fragment not attached or map not ready")
            return
        }
        val targetLocation = LatLng(lat, lng)
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(targetLocation, 15f),
            1000,
            null
        )
        Log.d("MapsFragment", "Camera animated to Lat=$lat, Lng=$lng")
    }

    fun animateCameraToBounds(locations: List<LatLng>) {
        if (!isAdded || !isMapReady) {
            Log.d("MapsFragment", "animateCameraToBounds skipped: Fragment not attached or map not ready")
            return
        }
        if (locations.isEmpty()) {
            Log.d("MapsFragment", "No locations provided for animateCameraToBounds")
            return
        }
        if (locations.size == 1) {
            animateCameraToLocation(locations[0].latitude, locations[0].longitude)
            return
        }

        val builder = LatLngBounds.Builder()
        locations.forEach { builder.include(it) }
        val bounds = builder.build()
        val padding = 50
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)

        mMap.animateCamera(
            cameraUpdate,
            1000,
            null
        )
        Log.d("MapsFragment", "Camera animated to bounds with ${locations.size} locations")
    }

    fun updateAngkotMarker(angkotId: Int, lat: Double, lng: Double) {
        if (!isAdded || !isMapReady) {
            Log.d("MapsFragment", "updateAngkotMarker skipped: Fragment not attached or map not ready")
            return
        }
        val position = LatLng(lat, lng)
        angkotMarkers[angkotId]?.remove()
        val marker = mMap.addMarker(
            MarkerOptions()
                .position(position)
                .title("Angkot $angkotId")
        )
        if (marker != null) {
            angkotMarkers[angkotId] = marker
        }
        Log.d("MapsFragment", "Marker updated for Angkot $angkotId at Lat=$lat, Lng=$lng")
    }

    fun clearAngkotMarkers() {
        if (!isAdded || !isMapReady) {
            Log.d("MapsFragment", "clearAngkotMarkers skipped: Fragment not attached or map not ready")
            return
        }
        angkotMarkers.values.forEach { it.remove() }
        angkotMarkers.clear()
        Log.d("MapsFragment", "All angkot markers cleared")
    }

    fun displayRoutePolylines(steps: List<StepsItem>) {
        if (!isAdded || !isMapReady) {
            Log.d("MapsFragment", "displayRoutePolylines skipped: Fragment not attached or map not ready")
            return
        }
        // Hapus polyline sebelumnya
        polylines.forEach { it.remove() }
        polylines.clear()

        val allPoints = mutableListOf<LatLng>()

        steps.forEach { step ->
            step.polyline?.let { encodedPolyline ->
                val points = PolyUtil.decode(encodedPolyline)
                allPoints.addAll(points)
                val color = when {
                    step.travelMode == "WALKING" -> android.graphics.Color.BLUE
                    step.transitDetails?.color != null -> try {
                        android.graphics.Color.parseColor(step.transitDetails.color)
                    } catch (e: IllegalArgumentException) {
                        android.graphics.Color.BLACK
                    }
                    else -> android.graphics.Color.BLACK
                }
                val polylineOptions = PolylineOptions()
                    .addAll(points)
                    .color(color)
                    .width(10f)
                // Tambahkan pola garis putus-putus untuk WALKING
                if (step.travelMode == "WALKING") {
                    polylineOptions.pattern(listOf(Dash(20f), Gap(10f)))
                    Log.d("MapsFragment", "Menambahkan polyline putus-putus untuk mode ${step.travelMode} dengan warna $color")
                } else {
                    Log.d("MapsFragment", "Menambahkan polyline solid untuk mode ${step.travelMode} dengan warna $color")
                }
                val polyline = mMap.addPolyline(polylineOptions)
                polylines.add(polyline)
            }
        }

        // Animasi kamera untuk menampilkan seluruh rute
        if (allPoints.isNotEmpty()) {
            animateCameraToBounds(allPoints)
        }
    }

    fun displayPolyline(encodedPolyline: String) {
        Log.d("MapsFragment", "displayPolyline called with polyline: $encodedPolyline")
        if (!isAdded || !isMapReady) {
            Log.d("MapsFragment", "displayPolyline skipped: Fragment not attached or map not ready")
            return
        }

        // Hapus polyline sebelumnya untuk menghindari duplikasi
        polylines.forEach { it.remove() }
        polylines.clear()

        try {
            // Dekode string polyline menjadi daftar LatLng
            val points = PolyUtil.decode(encodedPolyline)
            if (points.isEmpty()) {
                Log.d("MapsFragment", "No points decoded from polyline string")
                return
            }

            // Buat polyline options
            val polylineOptions = PolylineOptions()
                .addAll(points)
                .color(android.graphics.Color.BLACK) // Warna default
                .width(10f) // Lebar garis

            // Tambahkan polyline ke peta
            val polyline = mMap.addPolyline(polylineOptions)
            polylines.add(polyline)
            Log.d("MapsFragment", "Polyline added with ${points.size} points")

            // Animasi kamera untuk menampilkan seluruh polyline
            animateCameraToBounds(points)
        } catch (e: Exception) {
            Log.e("MapsFragment", "Error decoding polyline: ${e.message}")
            Toast.makeText(requireContext(), "Gagal menampilkan rute", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        permissionListener = null
        markerClickListener = null
        angkotMarkers.values.forEach { it.remove() }
        angkotMarkers.clear()
        polylines.forEach { it.remove() }
        polylines.clear()
        Log.d("MapsFragment", "onDestroyView called")
    }
}
package com.example.customerangkot.presentation.track.chooseangkot

import android.icu.text.NumberFormat
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.customerangkot.R
import com.example.customerangkot.databinding.FragmentChooseAngkotBinding
import com.example.customerangkot.di.ResultState
import com.example.customerangkot.di.ViewModelFactory
import com.example.customerangkot.presentation.buttonsheet.SumPassengerSheet
import com.example.customerangkot.presentation.maps.MapsFragment
import com.example.customerangkot.presentation.track.TrackAngkotViewModel
import com.example.customerangkot.utils.LocationPermissionListener
import com.example.customerangkot.utils.OnMarkerClickListener
import com.google.android.gms.maps.model.LatLng
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.Channel
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import org.json.JSONObject
import java.util.Locale


class ChooseAngkotFragment : Fragment(), LocationPermissionListener, OnMarkerClickListener {

    private var _binding: FragmentChooseAngkotBinding? = null
    private val binding get() = _binding!!

    private var trayekId: Int = 0
    private var startLat: Double = 0.0
    private var startLong: Double = 0.0
    private var destinationLat: Double = 0.0
    private var destinationLong: Double = 0.0
    private var price: Double = 0.0
    private var polyline: String = ""
    private var selectedAngkotId: Int? = null // Atribut baru untuk menyimpan angkotId yang dipilih

    private lateinit var pusher: Pusher
    private val subscribedChannels = mutableSetOf<Channel>()

    private val trackAngkotViewModel by viewModels<TrackAngkotViewModel> {
        ViewModelFactory.getInstance(requireContext())
    }

    private val TAG = "ChooseAngkotFragment" // [Baru] Tag untuk logging

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChooseAngkotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPusher()

        arguments?.let {
            trayekId = it.getInt("trayek_id", 0)
            startLat = it.getDouble("start_lat", 0.0)
            startLong = it.getDouble("start_long", 0.0)
            destinationLat = it.getDouble("destination_lat", 0.0)
            destinationLong = it.getDouble("destination_long", 0.0)
            price = it.getDouble("price", 0.0)
            polyline = it.getString("polyline", "")
            Log.d(TAG, "Data diterima: trayekId=$trayekId, startLat=$startLat, startLong=$startLong, destinationLat=$destinationLat, destinationLong=$destinationLong, price=$price, polyline=$polyline")
        }

        loadMaps()
        setupFAB()
        observeLocationState()
        observeAngkotState()
        observeAngkotPositions()

        if (trayekId != 0 && startLat != 0.0 && startLong != 0.0 && price != 0.0 && polyline.isNotEmpty()) {
            trackAngkotViewModel.getAngkotByTrayekId(startLat, startLong, trayekId)
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            binding.layoutPrice.setPrice(formatter.format(price).replace("Rp", "Rp. ").replace(",00", ""))

            // Amati status peta dari MapsFragment
            val mapsFragment = childFragmentManager.findFragmentById(R.id.map_real_time_angkot) as? MapsFragment
            if (mapsFragment != null) {
                Log.d(TAG, "Attempting to display polyline: $polyline")
                mapsFragment.mapReadyLiveData.observe(viewLifecycleOwner) { isReady ->
                    if (isReady) {
                        Log.d(TAG, "Map is ready, displaying polyline")
                        mapsFragment.displayPolyline(polyline)
                    } else {
                        Log.d(TAG, "Map not ready yet")
                    }
                }
            } else {
                Log.e(TAG, "MapsFragment not found")
            }
        } else {
            Log.e(TAG, "Invalid trayekId or coordinates: trayekId=$trayekId, startLat=$startLat, startLong=$startLong")
            Toast.makeText(requireContext(), "Data trayek atau lokasi tidak valid", Toast.LENGTH_LONG).show()
        }
    }

    private fun initPusher() {
        val options = PusherOptions().setCluster("ap1")
        pusher = Pusher("d1373b327727bf1ce9cf", options)
        pusher.connection.bind(ConnectionState.ALL, object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                Log.d(TAG, "Pusher state changed from ${change.previousState} to ${change.currentState}")
                if (change.currentState == ConnectionState.DISCONNECTED) {
                    pusher.connect()
                    Log.d(TAG, "Attempting to reconnect Pusher")
                }
            }

            override fun onError(message: String, code: String?, e: Exception?) {
                Log.e(TAG, "Pusher connection error: $message, code: $code, exception: ${e?.message}")
            }
        })
        pusher.connect()
        Log.d(TAG, "Pusher initialized")
    }

    private fun subscribeToAngkotChannels(angkotIds: List<Int>) {
        subscribedChannels.forEach { pusher.unsubscribe(it.name) }
        subscribedChannels.clear()

        angkotIds.forEach { angkotId ->
            val channelName = "angkot.$angkotId"
            val channel = pusher.subscribe(channelName)
            channel.bind("App\\Events\\AngkotLocationUpdated") { event ->
                try {
                    Log.d(TAG, "Received event on $channelName: ${event.data}")
                    val data = JSONObject(event.data)
                    val id = data.getInt("id")
                    val lat = data.getDouble("lat")
                    val lng = data.getDouble("long")
                    Log.d(TAG, "Received position update: id=$id, lat=$lat, lng=$lng")
                    if (isAdded) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Angkot $id updated: Lat=$lat, Lng=$lng",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    trackAngkotViewModel.updateAngkotPosition(id, lat, lng)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing Pusher message: ${e.message}")
                }
            }
            subscribedChannels.add(channel)
            Log.d(TAG, "Subscribed to channel: $channelName")
        }
    }

    private fun observeLocationState() {
        trackAngkotViewModel.locationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> {
                    showLoading(true)
                }
                is ResultState.Success -> {
                    val latLng = state.data
                    val mapsFragment = childFragmentManager.findFragmentById(R.id.map_real_time_angkot) as? MapsFragment
                    mapsFragment?.animateCameraToLocation(latLng.latitude, latLng.longitude)
                    showLoading(false)
                }
                is ResultState.Error -> {
                    showLoading(false)
                    Log.d(TAG, "Error mendapatkan lokasi: ${state.error}")
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun observeAngkotState() {
        trackAngkotViewModel.angkotState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> {
                    showLoading(true)
                }
                is ResultState.Success -> {
                    val angkotList = state.data
                    val mapsFragment = childFragmentManager.findFragmentById(R.id.map_real_time_angkot) as? MapsFragment
                    mapsFragment?.clearAngkotMarkers()
                    val locations = mutableListOf<LatLng>()
                    val angkotIds = mutableListOf<Int>()

                    angkotList.forEach { angkot ->
                        angkot.angkotId?.let { id ->
                            val lat = angkot.lat?.toDoubleOrNull()
                            val lng = angkot.long?.toDoubleOrNull()
                            if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                                mapsFragment?.updateAngkotMarker(id, lat, lng)
                                locations.add(LatLng(lat, lng))
                                angkotIds.add(id)
                            }
                        }
                    }

                    if (angkotIds.isNotEmpty()) {
                        subscribeToAngkotChannels(angkotIds)
                    }

                    if (locations.isNotEmpty()) {
                        mapsFragment?.animateCameraToBounds(locations)
                        Log.d(TAG, "Animating camera to show ${locations.size} markers for trayekId $trayekId")
                    } else {
                        mapsFragment?.animateCameraToLocation(startLat, startLong)
                        Log.d(TAG, "No markers, animating camera to start location")
                    }
                    showLoading(false)
                }
                is ResultState.Error -> {
                    showLoading(false)
                    Log.d(TAG, "Error mendapatkan angkot: ${state.error}")
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun observeAngkotPositions() {
        trackAngkotViewModel.angkotPositions.observe(viewLifecycleOwner) { positions ->
            val mapsFragment = childFragmentManager.findFragmentById(R.id.map_real_time_angkot) as? MapsFragment
            positions.forEach { (angkotId, latLng) ->
                mapsFragment?.updateAngkotMarker(angkotId, latLng.latitude, latLng.longitude)
                Log.d(TAG, "Updating marker for Angkot $angkotId: Lat=${latLng.latitude}, Lng=${latLng.longitude}")
            }
        }
    }

    private fun setupFAB() {
        binding.fabDone.setOnClickListener {
            if (selectedAngkotId == null) {
                Toast.makeText(requireContext(), "Pastikan anda memilih angkot terlebih dahulu", Toast.LENGTH_SHORT).show()
            } else {
                // [Berubah] Kirim data polyline ke SumPassengerSheet
                val sumPassengerSheet = SumPassengerSheet.newInstance(
                    driverId = selectedAngkotId,
                    startLat = startLat,
                    startLong = startLong,
                    destinationLat = destinationLat,
                    destinationLong = destinationLong,
                    price = price,
                    polyline = polyline // [Baru] Kirim polyline
                )
                sumPassengerSheet.show(parentFragmentManager, "SumPassengerSheet")
                Log.d(TAG, "Opening SumPassengerSheet with polyline: $polyline")
            }
        }
    }

    private fun loadMaps() {
        val layoutPositionMaps = binding.root.findViewById<View>(R.id.map_real_time_angkot)
        if (layoutPositionMaps == null) {
            Log.e(TAG, "FrameLayout with ID map_real_time_angkot not found in layout")
            return
        }
        val existingFragment = childFragmentManager.findFragmentById(R.id.map_real_time_angkot)
        if (existingFragment == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.map_real_time_angkot, MapsFragment())
                .commit()
            childFragmentManager.executePendingTransactions()
            Log.d(TAG, "MapsFragment attached")
        } else {
            Log.d(TAG, "MapsFragment already exists")
        }
    }

    override fun onLocationPermissionGranted() {
        trackAngkotViewModel.getUserLocation()
    }

    override fun onMarkerClicked(angkotId: Int) {
        selectedAngkotId = angkotId
        if (isAdded) {
            Toast.makeText(
                requireContext(),
                "Angkot dipilih: ID $angkotId",
                Toast.LENGTH_SHORT
            ).show()
            Log.d(TAG, "Angkot ID $angkotId dipilih")
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        subscribedChannels.forEach { pusher.unsubscribe(it.name) }
        pusher.disconnect()
        _binding = null
    }

    companion object {
        fun newInstance() = ChooseAngkotFragment()
    }
}
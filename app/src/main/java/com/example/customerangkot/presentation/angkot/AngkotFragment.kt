package com.example.customerangkot.presentation.angkot

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.customerangkot.R
import com.example.customerangkot.databinding.FragmentAngkotBinding
import com.example.customerangkot.di.ResultState
import com.example.customerangkot.di.ViewModelFactory
import com.example.customerangkot.domain.entity.InformationItem
import com.example.customerangkot.domain.entity.TrayekItem
import com.example.customerangkot.presentation.adapter.InformationAdapter
import com.example.customerangkot.presentation.adapter.TrayekAdapter
import com.example.customerangkot.presentation.card.LayoutPositionAngkot
import com.example.customerangkot.presentation.home.HomeViewModel
import com.example.customerangkot.presentation.maps.MapsFragment
import com.example.customerangkot.presentation.track.TrackAngkotActivity
import com.example.customerangkot.utils.HorizontalSpaceItemDecoration
import com.example.customerangkot.utils.LocationPermissionListener
import com.example.customerangkot.utils.Utils.getTrayekList
import com.google.android.gms.maps.model.LatLng
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.Channel
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import org.json.JSONObject

class AngkotFragment : Fragment(), LocationPermissionListener {

    private var _binding: FragmentAngkotBinding? = null
    private val binding get() = _binding!!
    private lateinit var pusher: Pusher
    private val subscribedChannels = mutableSetOf<Channel>()

    private var trayekAdapter: TrayekAdapter? = null

    private lateinit var trayekList: List<TrayekItem>

    private var lastCameraUpdateTime = 0L
    private var userLatitude: Double? = null // Baris tambahan: Simpan lokasi pengguna
    private var userLongitude: Double? = null

    private val angkotViewModel by viewModels<AngkotViewModel> {
        ViewModelFactory.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAngkotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        trayekList = getTrayekList(requireContext())


        initPusher()
//        setupSearchView()
        loadMaps()
        observeLocationState()
        setupSearching()
//        observeTrayekState()
        observeAngkotPositions()
        observeAngkotState()

        if (userLatitude != null && userLongitude != null) {
            angkotViewModel.getAngkotByTrayekId(userLatitude!!, userLongitude!!, 0)
        }


    }


    private fun showEmptyAngkot(isEmpty: Boolean) {
        binding.rvDetailAngkot.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.imageNoAngkot.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.textNoAngkot.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun setupSearching() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                trayekAdapter?.filter(newText) // ✅ aman
                return true
            }
        })
    }


    private fun initPusher() {
        val options = PusherOptions().setCluster("ap1")
        pusher = Pusher("d1373b327727bf1ce9cf", options)
        pusher.connection.bind(ConnectionState.ALL, object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                Log.d("AngkotFragment", "Pusher state changed from ${change.previousState} to ${change.currentState}")
                if (change.currentState == ConnectionState.DISCONNECTED) {
                    pusher.connect()
                    Log.d("AngkotFragment", "Attempting to reconnect Pusher")
                }
            }

            override fun onError(message: String, code: String?, e: Exception?) {
                Log.e("AngkotFragment", "Pusher connection error: $message, code: $code, exception: ${e?.message}")
            }
        })
        pusher.connect()
        Log.d("AngkotFragment", "Pusher initialized")
    }

    private fun subscribeToAngkotChannels(angkotIds: List<Int>) {
        subscribedChannels.forEach { pusher.unsubscribe(it.name) }
        subscribedChannels.clear()

        angkotIds.forEach { angkotId ->
            val channelName = "angkot.$angkotId"
            val channel = pusher.subscribe(channelName)
            channel.bind("App\\Events\\AngkotLocationUpdated") { event ->
                try {
                    Log.d("AngkotFragment", "Received event on $channelName: ${event.data}")
                    val data = JSONObject(event.data)
                    val id = data.getInt("id")
                    val lat = data.getDouble("lat")
                    val lng = data.getDouble("long")
                    Log.d("AngkotFragment", "Received position update: id=$id, lat=$lat, lng=$lng")
                    angkotViewModel.updateAngkotPosition(id, lat, lng)
                } catch (e: Exception) {
                    Log.e("AngkotFragment", "Error parsing Pusher message: ${e.message}")
                }
            }
            subscribedChannels.add(channel)
            Log.d("AngkotFragment", "Subscribed to channel: $channelName")
        }
    }

//    private fun setupSearchView() {
//        binding.searchView.apply {
//            setOnSearchClickListener {
//                navigateToTrackActivity()
//            }
//            setOnQueryTextFocusChangeListener { _, hasFocus ->
//                if (hasFocus) {
//                    navigateToTrackActivity()
//                }
//            }
//        }
//    }

//    private fun navigateToTrackActivity() {
//        val intent = Intent(requireActivity(), TrackAngkotActivity::class.java)
//        startActivity(intent)
//    }

    private fun loadMaps() {
        val layoutPositionMaps = binding.root.findViewById<View>(R.id.map_detail_trayek)
        if (layoutPositionMaps == null) {
            Log.e("AngkotFragment", "FrameLayout with ID position_maps not found in layout")
            return
        }
        val existingFragment = childFragmentManager.findFragmentById(R.id.map_detail_trayek)
        if (existingFragment == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.map_detail_trayek, MapsFragment())
                .commit()
            Log.d("AngkotFragment", "MapsFragment attached")
        } else {
            Log.d("AngkotFragment", "MapsFragment already exists")
        }
    }

    private fun setupRecycleView(trayekList: List<TrayekItem>, latitude: Double, longitude: Double, trayekIdFromIntent: Int) {
        trayekAdapter = TrayekAdapter(trayekList) { selectedTrayek ->
            val mapsFragment = childFragmentManager
                .findFragmentById(R.id.map_detail_trayek) as? MapsFragment

            if (selectedTrayek != null) {

                // SET trayek aktif
                angkotViewModel.setSelectedTrayek(selectedTrayek.trayekId)

                angkotViewModel.getAngkotByTrayekId(
                    latitude,
                    longitude,
                    selectedTrayek.trayekId
                )

                mapsFragment?.clearAngkotMarkers()
                subscribedChannels.forEach { pusher.unsubscribe(it.name) }
                subscribedChannels.clear()

            } else {

                angkotViewModel.setSelectedTrayek(null)
                angkotViewModel.clearAngkotState()

                mapsFragment?.clearAngkotMarkers()
                binding.rvDetailAngkot.adapter = null
                binding.rvDetailAngkot.visibility = View.GONE

                subscribedChannels.forEach { pusher.unsubscribe(it.name) }
                subscribedChannels.clear()
            }
        }

        if (trayekIdFromIntent != 0) {
            val selectedIndex = trayekList.indexOfFirst { it.trayekId == trayekIdFromIntent }
            if (selectedIndex != -1) {
                trayekAdapter?.setSelectedTrayekById(trayekIdFromIntent)
            }
        }

        binding.rvInformationTrayek.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = trayekAdapter
            val spacing = resources.getDimensionPixelSize(R.dimen.spacing_horizontal)
            addItemDecoration(HorizontalSpaceItemDecoration(spacing))
        }
    }

    private fun observeLocationState() {
        angkotViewModel.locationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> {
                    showLoading(true)
                }
                is ResultState.Success -> {
                    showLoading(false)
                    val latLng = state.data
                    userLatitude = latLng.latitude // Baris tambahan: Simpan lokasi pengguna
                    userLongitude = latLng.longitude
                    val mapsFragment = childFragmentManager.findFragmentById(R.id.map_detail_trayek) as? MapsFragment
                    mapsFragment?.animateCameraToLocation(latLng.latitude, latLng.longitude)
                    setupRecycleView(trayekList, userLatitude!!, userLongitude!!, 0)

                }
                is ResultState.Error -> {
                    showLoading(false)
                    Log.d("AngkotFragment", "Error mendapatkan lokasi: ${state.error}")
                }
            }
        }
    }

    private fun observeAngkotState() {

        angkotViewModel.selectedTrayekId.observe(viewLifecycleOwner) { trayekId ->

            // Kalau belum ada trayek terpilih → SEMBUNYIKAN list
            if (trayekId == null) {
                binding.rvDetailAngkot.adapter = null
                binding.rvDetailAngkot.visibility = View.GONE
                showEmptyAngkot(false)
                return@observe
            }
        }

        angkotViewModel.angkotState.observe(this) { state ->

            if (angkotViewModel.selectedTrayekId.value == null) {
                binding.rvDetailAngkot.adapter = null
                binding.rvDetailAngkot.visibility = View.GONE
                return@observe
            }

            when (state) {
                is ResultState.Loading -> {
                    binding.progressIndicator.visibility = View.VISIBLE
                    showEmptyAngkot(false)
                    binding.rvDetailAngkot.adapter = null
                    Log.d("DetailInformationTrayekActivity", "Loading angkot data, reset rvDetailAngkot")
                }
                is ResultState.Success -> {
                    val angkotList = state.data.map { item ->
                        InformationItem.AngkotInformation(
                            angkotId = item.angkotId ?: 0,
                            platNomor = item.platNomor ?: "Unknown",
                            distanceKm = (item.distanceKm as? Number)?.toDouble() ?: 0.0,
                            trayekId = item.trayek?.id ?: 0,
                            imageUrl = item.trayek?.imageUrl,
                            longtitude = item.long?.toDouble() ?: 0.0,
                            latitude = item.lat?.toDouble() ?: 0.0
                        )
                    }
                    val mapsFragment = childFragmentManager.findFragmentById(R.id.map_detail_trayek) as? MapsFragment
                    mapsFragment?.clearAngkotMarkers()
                    subscribedChannels.forEach { pusher.unsubscribe(it.name) }
                    subscribedChannels.clear()
                    Log.d("DetailInformationTrayekActivity", "Cleared previous Pusher subscriptions")

                    if (angkotList.isEmpty()) {
                        showEmptyAngkot(true)
                        binding.rvDetailAngkot.adapter = null
                        Log.d("DetailInformationTrayekActivity", "No angkot online, rvDetailAngkot set to empty")
                    } else {
                        showEmptyAngkot(false)

                        angkotList.forEach { angkot ->
                            if (angkot.latitude != 0.0 && angkot.longtitude != 0.0) {
                                mapsFragment?.updateAngkotMarker(
                                    angkot.angkotId,
                                    angkot.latitude,
                                    angkot.longtitude,
                                    angkot.platNomor  // [BARU] Kirim platNomor
                                )
                            }
                        }
                        angkotList.firstOrNull { it.latitude != 0.0 && it.longtitude != 0.0 }?.let { angkot ->
                            if (System.currentTimeMillis() - lastCameraUpdateTime > 2000) {
                                mapsFragment?.animateCameraToLocation(angkot.latitude, angkot.longtitude)
                                Log.d("DetailInformationTrayekActivity", "Camera animated to Angkot ${angkot.angkotId}: Lat=${angkot.latitude}, Lng=${angkot.longtitude}")
                                lastCameraUpdateTime = System.currentTimeMillis()
                            }
                        }
                        subscribeToAngkotChannels(angkotList.map { it.angkotId })



                        val informasiAdapter = InformationAdapter(angkotList) { selectedItem ->

                            when (selectedItem) {
                                is InformationItem.AngkotInformation -> {
                                    val latestPosition =
                                        angkotViewModel.angkotPositions.value?.get(selectedItem.angkotId)

                                    if (latestPosition != null) {
                                        mapsFragment?.animateCameraToLocation(
                                            latestPosition.latitude,
                                            latestPosition.longitude
                                        )
                                    } else {
                                        // fallback ke data awal (kalau realtime belum datang)
                                        mapsFragment?.animateCameraToLocation(
                                            selectedItem.latitude,
                                            selectedItem.longtitude
                                        )
                                    }
                                }
                                is InformationItem.TrayekInformation -> {
                                    // Isi
                                }
                            }
                        }
                        binding.rvDetailAngkot.apply {
                            layoutManager = LinearLayoutManager(context)
                            adapter = informasiAdapter
                        }
                    }
                    binding.progressIndicator.visibility = View.GONE
                }
                is ResultState.Error -> {
                    binding.progressIndicator.visibility = View.GONE
                    showEmptyAngkot(true)
                    binding.rvDetailAngkot.adapter = null
                    val mapsFragment = childFragmentManager.findFragmentById(R.id.map_detail_trayek) as? MapsFragment
                    mapsFragment?.clearAngkotMarkers()
                    subscribedChannels.forEach { pusher.unsubscribe(it.name) }
                    subscribedChannels.clear()
                    Log.d("DetailInformationTrayekActivity", "Error state, reset rvDetailAngkot and cleared markers/channels")
                }
            }
        }
    }

//    private fun observeTrayekState() {
//        angkotViewModel.trayekState.observe(viewLifecycleOwner) { state ->
//            when (state) {
//                is ResultState.Loading -> {
//                    showLoading(true)
//                }
//                is ResultState.Success -> {
//                    showLoading(false)
//                    val trayekList = state.data
//                    val mapsFragment = childFragmentManager.findFragmentById(R.id.map_detail_trayek) as? MapsFragment
//                    val trayekAdapter = TrayekAdapter(trayekList) { selectedTrayek ->
//                        if (selectedTrayek != null) {
//                            angkotViewModel.setSelectedAngkotIds(selectedTrayek.angkotIds)
//                            mapsFragment?.clearAngkotMarkers()
//                            val locations = mutableListOf<LatLng>()
//                            selectedTrayek.angkotIds.forEachIndexed { index, angkotId ->
//                                if (index < selectedTrayek.latitudes.size &&
//                                    index < selectedTrayek.longitudes.size &&
//                                    index < selectedTrayek.platNomors.size) {
//                                    val lat = selectedTrayek.latitudes[index]
//                                    val lng = selectedTrayek.longitudes[index]
//                                    val platNomor = selectedTrayek.platNomors[index]
//                                    if (lat != 0.0 && lng != 0.0) {
//                                        mapsFragment?.updateAngkotMarker(angkotId, lat, lng, platNomor)
//                                        locations.add(LatLng(lat, lng))
//                                    }
//                                }
//                            }
//                            subscribeToAngkotChannels(selectedTrayek.angkotIds)
//                            // Baris tambahan: Animasi kamera ke semua marker
//                            if (locations.isNotEmpty()) {
//                                mapsFragment?.animateCameraToBounds(locations)
//                                Log.d("AngkotFragment", "Animating camera to show ${locations.size} markers for trayek ${selectedTrayek.name}")
//                            } else if (userLatitude != null && userLongitude != null) {
//                                mapsFragment?.animateCameraToLocation(userLatitude!!, userLongitude!!)
//                                Log.d("AngkotFragment", "No markers, animating camera to user location")
//                            }
//                        } else {
//                            angkotViewModel.setSelectedAngkotIds(null)
//                            mapsFragment?.clearAngkotMarkers()
//                            subscribedChannels.forEach { pusher.unsubscribe(it.name) }
//                            subscribedChannels.clear()
//                            // Baris tambahan: Kembalikan kamera ke lokasi pengguna saat deselect
//                            if (userLatitude != null && userLongitude != null) {
//                                mapsFragment?.animateCameraToLocation(userLatitude!!, userLongitude!!)
//                                Log.d("AngkotFragment", "Deselected trayek, animated camera to user location")
//                            }
//                            Log.d("AngkotFragment", "Deselected trayek, cleared markers and unsubscribed channels")
//                        }
//                    }
//                    binding.rvTrayek.apply {
//                        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
//                        adapter = trayekAdapter
//                        val spacing = resources.getDimensionPixelSize(R.dimen.spacing_horizontal)
//                        addItemDecoration(HorizontalSpaceItemDecoration(spacing))
//                    }
//
//                }
//                is ResultState.Error -> {
//                    showLoading(false)
//                    Log.d("AngkotFragment", "Error mendapatkan trayek: ${state.error}")
//                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
//                }
//            }
//        }
//    }

    private fun observeAngkotPositions() {
        Log.d("AngkotFragment", "Observing angkotPositions")
        angkotViewModel.angkotPositions.observe(viewLifecycleOwner) { positions ->
            val mapsFragment = childFragmentManager.findFragmentById(R.id.map_detail_trayek) as? MapsFragment
            positions.forEach { (angkotId, latLng) ->
                mapsFragment?.updateAngkotMarker(angkotId, latLng.latitude, latLng.longitude)
                Log.d("AngkotFragment", "Updating marker for Angkot $angkotId: Lat=${latLng.latitude}, Lng=${latLng.longitude}")
            }
        }
    }

    override fun onLocationPermissionGranted() {
        angkotViewModel.getUserLocation()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        angkotViewModel.setSelectedTrayek(null)
        angkotViewModel.clearAngkotState()

        trayekAdapter = null
        subscribedChannels.forEach { pusher.unsubscribe(it.name) }
        pusher.disconnect()
        _binding = null
    }
}
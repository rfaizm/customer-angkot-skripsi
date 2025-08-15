package com.example.customerangkot.presentation.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customerangkot.R
import com.example.customerangkot.data.preference.UserPreference
import com.example.customerangkot.data.preference.dataStore
import com.example.customerangkot.databinding.FragmentHomeBinding
import com.example.customerangkot.di.ResultState
import com.example.customerangkot.di.ViewModelFactory
import com.example.customerangkot.domain.entity.InformationItem
import com.example.customerangkot.presentation.adapter.InformationAdapter
import com.example.customerangkot.presentation.adapter.TrayekAdapter
import com.example.customerangkot.presentation.card.LayoutPositionAngkot
import com.example.customerangkot.presentation.informationtrayek.DetailInformationTrayekActivity
import com.example.customerangkot.presentation.maps.MapsFragment
import com.example.customerangkot.presentation.topup.TopUpActivity
import com.example.customerangkot.utils.HorizontalSpaceItemDecoration
import com.example.customerangkot.utils.InformationTrayek
import com.example.customerangkot.utils.LocationPermissionListener
import com.example.customerangkot.utils.Utils
import com.google.android.gms.maps.model.LatLng
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.Channel
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import kotlinx.coroutines.launch
import org.json.JSONObject

class HomeFragment : Fragment(), LocationPermissionListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var userLatitude: Double? = null
    private var userLongitude: Double? = null
    private lateinit var pusher: Pusher
    private val subscribedChannels = mutableSetOf<Channel>()

    private val homeViewModel by viewModels<HomeViewModel> {
        ViewModelFactory.getInstance(requireContext())
    }

    private val userPreference by lazy {
        UserPreference.getInstance(requireContext().dataStore)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPusher()
        attachMaps()
        goToTopup()
        setupButton()
        setupUsername()

        observeLocationState()
        observeTrayekState()
        observeAllTrayekState()
        observeAngkotPositions()
        observeGetSaldo()

        // Panggil endpoint /saldo saat fragment dimuat
        homeViewModel.getSaldo()

    }

    private fun setupUsername() {
        val name = userPreference.getName()

        Log.d("HomeFragment", "User profile: name=$name")

        if (name != null) {
            binding.welcomeText.text = "Selamat Datang, $name"
        } else {
            binding.welcomeText.text = "Selamat Datang, User"
        }
    }

    private fun initPusher() {
        val options = PusherOptions().setCluster("ap1")
        pusher = Pusher("d1373b327727bf1ce9cf", options)
        pusher.connection.bind(ConnectionState.ALL, object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                Log.d("HomeFragment", "Pusher state changed from ${change.previousState} to ${change.currentState}")
                if (change.currentState == ConnectionState.DISCONNECTED) {
                    pusher.connect()
                }
            }

            override fun onError(message: String, code: String?, e: Exception?) {
                Log.e("HomeFragment", "Pusher connection error: $message, code: $code, exception: ${e?.message}")
            }
        })
        pusher.connect()
    }

    private fun subscribeToAngkotChannels(angkotIds: List<Int>) {
        subscribedChannels.forEach { pusher.unsubscribe(it.name) }
        subscribedChannels.clear()

        angkotIds.forEach { angkotId ->
            val channelName = "angkot.$angkotId"
            val channel = pusher.subscribe(channelName)
            channel.bind("App\\Events\\AngkotLocationUpdated") { event ->
                try {
                    Log.d("HomeFragment", "Received event on $channelName: ${event.data}")
                    val data = JSONObject(event.data)
                    val id = data.getInt("id")
                    val lat = data.getDouble("lat")
                    val lng = data.getDouble("long")
                    Log.d("HomeFragment", "Received position update: id=$id, lat=$lat, lng=$lng")
                    if (isAdded) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Angkot $id updated: Lat=$lat, Lng=$lng",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    homeViewModel.updateAngkotPosition(id, lat, lng)
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Error parsing Pusher message: ${e.message}")
                }
            }
            subscribedChannels.add(channel)
            Log.d("HomeFragment", "Subscribed to channel: $channelName")
        }
    }

    private fun goToTopup() {
        binding.logoTopup.setOnClickListener {
            val intent = Intent(requireContext(), TopUpActivity::class.java)
            startActivity(intent)
        }
    }



    private fun setupButton() {
        binding.detailToInformasi.setOnClickListener {
            val intent = Intent(requireContext(), DetailInformationTrayekActivity::class.java).apply {
                userLatitude?.let { putExtra("LAT_USER", it) }
                userLongitude?.let { putExtra("LONG_USER", it) }
            }
            startActivity(intent)
        }
    }

    private fun attachMaps() {
        val layoutPositionAngkot = binding.root.findViewById<LayoutPositionAngkot>(R.id.position_angkot)

        val existingFragment = childFragmentManager.findFragmentById(layoutPositionAngkot.frameMaps.id)
        if (existingFragment == null) {
            childFragmentManager.beginTransaction()
                .replace(layoutPositionAngkot.frameMaps.id, MapsFragment())
                .commit()
        }
    }

    private fun observeLocationState() {
        homeViewModel.locationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> {
                    showLoading(true)
                }
                is ResultState.Success -> {
                    val latLng = state.data
                    userLatitude = latLng.latitude
                    userLongitude = latLng.longitude
                    Toast.makeText(
                        requireContext(),
                        "Lokasi: Lat=${latLng.latitude}, Lng=${latLng.longitude}",
                        Toast.LENGTH_LONG
                    ).show()
                    val mapsFragment = childFragmentManager.findFragmentById(
                        binding.root.findViewById<LayoutPositionAngkot>(R.id.position_angkot).frameMaps.id
                    ) as? MapsFragment
                    mapsFragment?.animateCameraToLocation(latLng.latitude, latLng.longitude)
                    homeViewModel.getClosestTrayek(latLng.latitude, latLng.longitude)
                    showLoading(false)
                }
                is ResultState.Error -> {
                    showLoading(false)
                    Log.d("HomeFragment", "Error mendapatkan lokasi: ${state.error}")
                }
            }
        }
    }

    private fun observeTrayekState() {
        homeViewModel.trayekState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> {
                    showLoading(true)
                }
                is ResultState.Success -> {
                    val trayekList = state.data
                    val mapsFragment = childFragmentManager.findFragmentById(
                        binding.root.findViewById<LayoutPositionAngkot>(R.id.position_angkot).frameMaps.id
                    ) as? MapsFragment
                    val trayekAdapter = TrayekAdapter(trayekList) { selectedTrayek ->
                        if (selectedTrayek != null) {
                            homeViewModel.setSelectedAngkotIds(selectedTrayek.angkotIds)
                            mapsFragment?.clearAngkotMarkers()
                            val locations = mutableListOf<LatLng>()
                            selectedTrayek.angkotIds.forEachIndexed { index, angkotId ->
                                if (index < selectedTrayek.latitudes.size && index < selectedTrayek.longitudes.size) {
                                    val lat = selectedTrayek.latitudes[index]
                                    val lng = selectedTrayek.longitudes[index]
                                    if (lat != 0.0 && lng != 0.0) {
                                        mapsFragment?.updateAngkotMarker(angkotId, lat, lng)
                                        locations.add(LatLng(lat, lng))
                                    }
                                }
                            }
                            subscribeToAngkotChannels(selectedTrayek.angkotIds)
                            // Baris tambahan: Animasi kamera ke semua marker
                            if (locations.isNotEmpty()) {
                                mapsFragment?.animateCameraToBounds(locations)
                                Log.d("HomeFragment", "Animating camera to show ${locations.size} markers for trayek ${selectedTrayek.name}")
                            } else if (userLatitude != null && userLongitude != null) {
                                mapsFragment?.animateCameraToLocation(userLatitude!!, userLongitude!!)
                                Log.d("HomeFragment", "No markers, animating camera to user location")
                            }
                        } else {
                            homeViewModel.setSelectedAngkotIds(null)
                            mapsFragment?.clearAngkotMarkers()
                            subscribedChannels.forEach { pusher.unsubscribe(it.name) }
                            subscribedChannels.clear()
                            // Baris tambahan: Kembalikan kamera ke lokasi pengguna saat deselect
                            if (userLatitude != null && userLongitude != null) {
                                mapsFragment?.animateCameraToLocation(userLatitude!!, userLongitude!!)
                                Log.d("HomeFragment", "Deselected trayek, animated camera to user location")
                            }
                            Log.d("HomeFragment", "Deselected trayek, cleared markers and unsubscribed channels")
                        }
                    }
                    val layoutPositionAngkot = binding.root.findViewById<LayoutPositionAngkot>(R.id.position_angkot)
                    layoutPositionAngkot.findViewById<RecyclerView>(R.id.rv_trayek).apply {
                        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                        adapter = trayekAdapter
                        val spacing = resources.getDimensionPixelSize(R.dimen.spacing_horizontal)
                        addItemDecoration(HorizontalSpaceItemDecoration(spacing))
                    }
                    showLoading(false)
                }
                is ResultState.Error -> {
                    showLoading(false)
                    Log.d("HomeFragment", "Error mendapatkan trayek: ${state.error}")
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun observeAllTrayekState() {
        homeViewModel.allTrayekState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> {
                    showLoading(true)
                }
                is ResultState.Success -> {
                    // Baris 222-237: Filter trayek unik berdasarkan trayekId
                    val uniqueTrayeks = state.data
                        .filter { it.trayek?.id != null } // Pastikan trayekId tidak null
                        .groupBy { it.trayek!!.id } // Kelompokkan berdasarkan trayekId
                        .map { (_, items) ->
                            val firstItem = items.first() // Ambil item pertama untuk setiap trayekId
                            InformationItem.TrayekInformation(
                                name = firstItem.trayek?.name ?: "",
                                description = firstItem.trayek?.description ?: "",
                                trayekId = firstItem.trayek?.id ?: 0,
                                imageUrl = firstItem.trayek?.imageUrl
                            )
                        }
                    Log.d("HomeFragment", "Unique trayeks displayed: ${uniqueTrayeks.size} items")

                    val informasiAdapter = InformationAdapter(uniqueTrayeks) { selectedItem ->
                        val intent = Intent(requireContext(), DetailInformationTrayekActivity::class.java).apply {
                            when (selectedItem) {
                                is InformationItem.AngkotInformation -> {
                                    putExtra("ID_TRAYEK", selectedItem.trayekId)
                                }
                                is InformationItem.TrayekInformation -> {
                                    putExtra("ID_TRAYEK", selectedItem.trayekId)
                                }
                            }
                            userLatitude?.let { putExtra("LAT_USER", it) }
                            userLongitude?.let { putExtra("LONG_USER", it) }
                        }
                        startActivity(intent)
                    }
                    binding.informasiAngkot.apply {
                        layoutManager = LinearLayoutManager(context)
                        adapter = informasiAdapter
                    }
                    showLoading(false)
                }
                is ResultState.Error -> {
                    showLoading(false)
                    Log.d("HomeFragment", "Error mendapatkan trayek: ${state.error}")
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun observeGetSaldo() {
        homeViewModel.getSaldo.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> {
                    showLoading(true)
                }
                is ResultState.Success -> {
                    if (state.data.data?.saldo != null) {
                        binding.saldoRupiah.text = Utils.formatNumber(state.data.data.saldo)
                    } else {
                        Log.e("HomeFragment", "Saldo data is null")
                    }
                }
                is ResultState.Error -> {
                    showLoading(false)
                    Log.d("HomeFragment", "Error mendapatkan saldo: ${state.error}")
                }
            }
        }
    }

    private fun observeAngkotPositions() {
        homeViewModel.angkotPositions.observe(viewLifecycleOwner) { positions ->
            val mapsFragment = childFragmentManager.findFragmentById(
                binding.root.findViewById<LayoutPositionAngkot>(R.id.position_angkot).frameMaps.id
            ) as? MapsFragment
            positions.forEach { (angkotId, latLng) ->
                mapsFragment?.updateAngkotMarker(angkotId, latLng.latitude, latLng.longitude)
                Log.d("HomeFragment", "Updating marker for Angkot $angkotId: Lat=${latLng.latitude}, Lng=${latLng.longitude}")
            }
        }
    }

    override fun onLocationPermissionGranted() {
        homeViewModel.getUserLocation()
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
        fun newInstance() = HomeFragment()
    }
}
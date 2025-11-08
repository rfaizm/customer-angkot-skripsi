package com.example.customerangkot.presentation.track.map

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.customerangkot.R
import com.example.customerangkot.data.api.dto.TrayeksItem
import com.example.customerangkot.databinding.FragmentMapRouteBinding
import com.example.customerangkot.di.ResultState
import com.example.customerangkot.di.ViewModelFactory
import com.example.customerangkot.presentation.adapter.RouteAdapter
import com.example.customerangkot.presentation.angkot.AngkotViewModel
import com.example.customerangkot.presentation.maps.ChooseLocationMapsFragment
import com.example.customerangkot.presentation.maps.MapsFragment
import com.example.customerangkot.presentation.track.TrackAngkotViewModel
import com.example.customerangkot.presentation.track.chooseangkot.ChooseAngkotFragment
import com.example.customerangkot.utils.LocationPermissionListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.customerangkot.utils.RouteAngkot
import java.text.NumberFormat
import kotlin.math.abs

class MapRouteFragment : Fragment(), LocationPermissionListener {
    private var _binding: FragmentMapRouteBinding? = null
    private val binding get() = _binding!!
    private var startLong: Double? = null
    private var startLat: Double? = null
    private var destinationLong: Double? = null
    private var destinationLat: Double? = null
    private var routeType: String = "best_route"
    private var isLocationInitialized: Boolean = false
    private var startPlaceName: String? = null
    private var destinationPlaceName: String? = null

    private val trackAngkotViewModel by viewModels<TrackAngkotViewModel> {
        ViewModelFactory.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapRouteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            startPlaceName = it.getString("start_place_name")
            destinationPlaceName = it.getString("destination_place_name")
            startLat = it.getDouble("start_lat", Double.NaN).takeIf { !it.isNaN() }
            startLong = it.getDouble("start_long", Double.NaN).takeIf { !it.isNaN() }
            destinationLat = it.getDouble("destination_lat", Double.NaN).takeIf { !it.isNaN() }
            destinationLong = it.getDouble("destination_long", Double.NaN).takeIf { !it.isNaN() }
            routeType = it.getString("route_type", "best_route")
            isLocationInitialized = it.getBoolean("is_location_initialized")
        }

        startPlaceName?.let {
            binding.searchViewStart.post {
                binding.searchViewStart.setQuery(it, false)
                Log.d("MapRouteFragment", "Memulihkan searchViewStart: $it")
            }
        }
        destinationPlaceName?.let {
            binding.searchViewDestination.post {
                binding.searchViewDestination.setQuery(it, false)
                Log.d("MapRouteFragment", "Memulihkan searchViewDestination: $it")
            }
        }

        loadMaps()
        setupRecycleView()
        setupFAB()
        setupSearchView()
        setupRadioGroup()
        setupLocationResultListener()

        observeLocationState()
        observePlaceNameState()
        observeRoutesState()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("start_place_name", startPlaceName)
        outState.putString("destination_place_name", destinationPlaceName)
        outState.putDouble("start_lat", startLat ?: Double.NaN)
        outState.putDouble("start_long", startLong ?: Double.NaN)
        outState.putDouble("destination_lat", destinationLat ?: Double.NaN)
        outState.putDouble("destination_long", destinationLong ?: Double.NaN)
        outState.putString("route_type", routeType)
        outState.putBoolean("is_location_initialized", isLocationInitialized)
    }

    private fun setupRadioGroup() {
        binding.radioGroupRouteType.setOnCheckedChangeListener { _, checkedId ->
            routeType = when (checkedId) {
                R.id.radio_best_route -> "best_route"
                R.id.radio_less_transit -> "less_transit"
                R.id.radio_less_walking -> "less_walking"
                else -> "best_route"
            }
            Log.d("MapRouteFragment", "Route type selected: $routeType")
        }
    }

    private fun setupLocationResultListener() {
        setFragmentResultListener("location_result:start") { _, bundle ->
            val latitude = bundle.getDouble("latitude")
            val longitude = bundle.getDouble("longitude")
            startLat = latitude
            startLong = longitude
            Log.d("MapRouteFragment", "Start location received: Lat=$latitude, Long=$longitude")
            trackAngkotViewModel.getPlaceName(latitude, longitude, "start")
        }

        setFragmentResultListener("location_result:destination") { _, bundle ->
            val latitude = bundle.getDouble("latitude")
            val longitude = bundle.getDouble("longitude")
            destinationLat = latitude
            destinationLong = longitude
            Log.d("MapRouteFragment", "Destination location received: Lat=$latitude, Long=$longitude")
            trackAngkotViewModel.getPlaceName(latitude, longitude, "destination")
        }
    }

    private fun observeLocationState() {
        trackAngkotViewModel.locationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> showLoading(true)
                is ResultState.Success -> {
                    val latLng = state.data
                    Log.d("MapRouteFragment", "Lokasi berhasil: Lat=${latLng.latitude}, Lng=${latLng.longitude}")
                    if (!isLocationInitialized && startLat == null && startLong == null) {
                        startLat = latLng.latitude
                        startLong = latLng.longitude
                        trackAngkotViewModel.getPlaceName(latLng.latitude, latLng.longitude, "start")
                        isLocationInitialized = true
                    }
                    val mapsFragment = childFragmentManager.findFragmentById(R.id.tracker_map) as? MapsFragment
                    mapsFragment?.animateCameraToLocation(latLng.latitude, latLng.longitude)
                    showLoading(false)
                }
                is ResultState.Error -> {
                    showLoading(false)
                    Log.e("MapRouteFragment", "Error mendapatkan lokasi: ${state.error}")
                    Toast.makeText(requireContext(), "Error lokasi: ${state.error}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observePlaceNameState() {
        trackAngkotViewModel.placeNameState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> showLoading(true)
                is ResultState.Success -> {
                    val placeName = state.data.data?.placeName ?: "Lokasi tidak dikenal"
                    Log.d("MapRouteFragment", "Nama tempat diterima: $placeName")
                    when (trackAngkotViewModel.lastLocationType) {
                        "start" -> {
                            startPlaceName = placeName
                            Log.d("MapRouteFragment", "Memperbarui searchViewStart dengan: $placeName")
                            binding.searchViewStart.post {
                                binding.searchViewStart.setQuery(placeName, false)
                                Log.d("MapRouteFragment", "searchViewStart query setelah update: ${binding.searchViewStart.query}")
                            }
                        }
                        "destination" -> {
                            destinationPlaceName = placeName
                            Log.d("MapRouteFragment", "Memperbarui searchViewDestination dengan: $placeName")
                            binding.searchViewDestination.post {
                                binding.searchViewDestination.setQuery(placeName, false)
                                Log.d("MapRouteFragment", "searchViewDestination query setelah update: ${binding.searchViewDestination.query}")
                            }
                            startPlaceName?.let {
                                binding.searchViewStart.post {
                                    binding.searchViewStart.setQuery(it, false)
                                    Log.d("MapRouteFragment", "Memulihkan searchViewStart: $it")
                                }
                            }
                        }
                        else -> Log.w("MapRouteFragment", "Tipe lokasi tidak diketahui")
                    }
                    showLoading(false)
                }
                is ResultState.Error -> {
                    showLoading(false)
                    Log.e("MapRouteFragment", "Error mendapatkan nama tempat: ${state.error}")
                    Toast.makeText(requireContext(), "Error nama tempat: ${state.error}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeRoutesState() {
        trackAngkotViewModel.routesState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> showLoading(true)
                is ResultState.Success -> {
                    val routeData = state.data.data?.firstOrNull()
                    if (routeData != null) {
                        val mapsFragment = childFragmentManager.findFragmentById(R.id.tracker_map) as? MapsFragment
                        mapsFragment?.displayRoutePolylines(routeData.steps?.filterNotNull() ?: emptyList())
                        val trayeks = routeData.trayeks?.filterNotNull() ?: emptyList()
                        updateRouteAdapter(trayeks)
                    } else {
                        Toast.makeText(requireContext(), "Tidak ada rute yang ditemukan", Toast.LENGTH_SHORT).show()
                    }
                    showLoading(false)
                }
                is ResultState.Error -> {
                    showLoading(false)
                    Log.e("MapRouteFragment", "Error mendapatkan rute: ${state.error}")
                    Toast.makeText(requireContext(), "Error rute: ${state.error}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateRouteAdapter(trayeks: List<TrayeksItem>) {
        val routeList = trayeks.map {
            RouteAngkot(
                trayekId = it.id ?: 0,
                namaTrayek = it.name ?: "Trayek Tidak Dikenal",
                predictETA = it.duration ?: "N/A",
                price = it.price?.toDouble() ?: 0.0,
                polyline = it.polyline.toString(),
                startLat = it.startLat ?: 0.0,
                startLong = it.startLong ?: 0.0,
                destinationLat = it.destinationLat ?: 0.0,
                destinationLong = it.destinationLong ?: 0.0
            )
        }
        val routeAdapter = RouteAdapter(routeList, { selectedRoute ->
            val trayekItem = trayeks.find { it.id == selectedRoute.trayekId }
            val departureLocation = trayekItem?.departure ?: "lokasi tujuan"

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Konfirmasi Posisi")
                .setMessage("Pastikan anda sudah diposisi $departureLocation")
                .setPositiveButton("Ya") { _, _ ->
                    // Navigasi ke ChooseAngkotFragment dengan data
                    val bundle = Bundle().apply {
                        putInt("trayek_id", selectedRoute.trayekId)
                        putDouble("start_lat", selectedRoute.startLat)
                        putDouble("start_long", selectedRoute.startLong)
                        putDouble("destination_lat", selectedRoute.destinationLat)
                        putDouble("destination_long", selectedRoute.destinationLong)
                        putDouble("price", selectedRoute.price)
                        putString("polyline", selectedRoute.polyline)
                    }
                    val chooseAngkotFragment = ChooseAngkotFragment.newInstance().apply {
                        arguments = bundle
                    }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container, chooseAngkotFragment)
                        .addToBackStack("ChooseAngkotFragment")
                        .commit()
                    Log.d("MapRouteFragment", "Navigasi ke ChooseAngkotFragment dengan trayekId: ${selectedRoute.trayekId}")
                }
                .setNegativeButton("Batal", null)
                .show()
        })
        binding.rvTrackRoute.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = routeAdapter
        }
    }

    private fun setupSearchView() {
        binding.btnMapStart.setOnClickListener {
            navigateToMapsFragment("start")
        }
        binding.searchViewStart.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) navigateToMapsFragment("start")
        }
        binding.searchViewStart.setOnSearchClickListener {
            navigateToMapsFragment("start")
        }
        binding.btnMapDestination.setOnClickListener {
            navigateToMapsFragment("destination")
        }
        binding.searchViewDestination.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) navigateToMapsFragment("destination")
        }
        binding.searchViewDestination.setOnSearchClickListener {
            navigateToMapsFragment("destination")
        }
    }

    private fun navigateToMapsFragment(locationType: String) {
        val mapsFragment = ChooseLocationMapsFragment.newInstance(locationType)
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, mapsFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setupRecycleView() {
        binding.rvTrackRoute.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = RouteAdapter(emptyList(), { _,-> })
        }
    }

    private fun loadMaps() {
        val layoutPositionMaps = binding.root.findViewById<View>(R.id.tracker_map)
        if (layoutPositionMaps == null) {
            Log.e("MapRouteFragment", "FrameLayout with ID position_maps not found in layout")
            return
        }
        val existingFragment = childFragmentManager.findFragmentById(R.id.tracker_map)
        if (existingFragment == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.tracker_map, MapsFragment())
                .commit()
            Log.d("MapRouteFragment", "MapsFragment attached")
        } else {
            Log.d("MapRouteFragment", "MapsFragment already exists")
        }
    }

    private fun setupFAB() {
        binding.fabReady.setOnClickListener {
            Log.d("MapRouteFragment", "Start: Lat=$startLat, Long=$startLong")
            Log.d("MapRouteFragment", "Destination: Lat=$destinationLat, Long=$destinationLong")
            Log.d("MapRouteFragment", "Route type: $routeType")
            if (startLat != null && startLong != null && destinationLat != null && destinationLong != null) {
                trackAngkotViewModel.getRoutes(
                    startLat!!, startLong!!, destinationLat!!, destinationLong!!, routeType
                )
            } else {
                Toast.makeText(requireContext(), "Pilih lokasi awal dan tujuan terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onLocationPermissionGranted() {
        if (!isLocationInitialized) {
            Log.d("MapRouteFragment", "Izin lokasi diberikan")
            trackAngkotViewModel.getUserLocation()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = MapRouteFragment()
    }
}
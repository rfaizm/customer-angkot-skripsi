package com.example.customerangkot.presentation.track.waitingangkot

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.example.customerangkot.MainActivity
import com.example.customerangkot.R
import com.example.customerangkot.databinding.FragmentWaitingAngkotBinding
import com.example.customerangkot.di.ResultState
import com.example.customerangkot.di.ViewModelFactory
import com.example.customerangkot.presentation.maps.MapsFragment
import com.example.customerangkot.presentation.track.TrackAngkotViewModel
import com.example.customerangkot.presentation.track.map.MapRouteFragment
import com.example.customerangkot.utils.LocationPermissionListener
import com.google.maps.android.PolyUtil
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.Channel
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import org.json.JSONObject


class WaitingAngkotFragment : Fragment(), LocationPermissionListener {

    private var _binding: FragmentWaitingAngkotBinding? = null
    private val binding get() = _binding!!

    // Atribut untuk menyimpan data dari SumPassengerSheet
    private var driverId: Int = 0
    private var startLat: Double = 0.0
    private var startLong: Double = 0.0
    private var destinationLat: Double = 0.0
    private var destinationLong: Double = 0.0
    private var numberOfPassengers: Int = 0
    private var totalPrice: Double = 0.0
    private var orderId: Int? = null
    private var polyline: String = ""

    private lateinit var pusher: Pusher
    private val subscribedChannels = mutableSetOf<Channel>()

    private val trackAngkotViewModel by viewModels<TrackAngkotViewModel> {
        ViewModelFactory.getInstance(requireContext())
    }

    private val TAG = "WaitingAngkotFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWaitingAngkotBinding.inflate(inflater, container, false)

        // Ambil data dari arguments
        arguments?.let {
            driverId = it.getInt("driver_id", 0)
            startLat = it.getDouble("start_lat", 0.0)
            startLong = it.getDouble("start_long", 0.0)
            destinationLat = it.getDouble("destination_lat", 0.0)
            destinationLong = it.getDouble("destination_long", 0.0)
            numberOfPassengers = it.getInt("number_of_passengers", 0)
            totalPrice = it.getDouble("total_price", 0.0)
            polyline = it.getString("polyline", "")
            Log.d(TAG, "Data diterima: driverId=$driverId, startLat=$startLat, startLong=$startLong, destinationLat=$destinationLat, destinationLong=$destinationLong, numberOfPassengers=$numberOfPassengers, totalPrice=$totalPrice, polyline=$polyline")
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initPusher()
        loadMaps()
        observeOrderState()
        observeCancelOrderState()
        observeETAState()

        // Validasi data sebelum memanggil endpoint
        if (driverId == 0 || numberOfPassengers <= 0 || totalPrice <= 0.0) {
            Toast.makeText(requireContext(), "Data pesanan tidak valid", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Data tidak valid: driverId=$driverId, numberOfPassengers=$numberOfPassengers, totalPrice=$totalPrice")
            return
        }

        // Panggil endpoint /create-order
        trackAngkotViewModel.createOrder(
            driverId = driverId,
            startLat = startLat,
            startLong = startLong,
            destinationLat = destinationLat,
            destinationLong = destinationLong,
            numberOfPassengers = numberOfPassengers,
            totalPrice = totalPrice
        )

        // Setup listener tombol batal
        binding.layoutCancelAngkotWait.setOnCancelClickListener {
            orderId?.let { id ->
                Log.d(TAG, "Cancel button clicked for orderId=$id")
                trackAngkotViewModel.cancelOrder(id)
            } ?: run {
                Log.e(TAG, "Order ID tidak tersedia untuk pembatalan")
                Toast.makeText(requireContext(), "Pesanan tidak valid untuk dibatalkan", Toast.LENGTH_SHORT).show()
            }
        }

        // Tambahkan callback untuk menangani tombol back
        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Kosongkan ini untuk memblokir tombol back
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            backCallback
        )
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

        // Berlangganan ke channel posisi angkot
        subscribeToAngkotPosition()
    }

    private fun subscribeToAngkotPosition() {
        val channelName = "angkot.$driverId"
        val channel = pusher.subscribe(channelName)
        channel.bind("App\\Events\\AngkotLocationUpdated") { event ->
            try {
                Log.d(TAG, "Received event on $channelName: ${event.data}")
                val data = JSONObject(event.data)
                val id = data.getInt("id")
                val lat = data.getDouble("lat")
                val lng = data.getDouble("long")
                if (isAdded) {
                    requireActivity().runOnUiThread {
                        val mapsFragment = childFragmentManager.findFragmentById(R.id.map_waiting_angkot) as? MapsFragment
                        mapsFragment?.updateAngkotMarker(id, lat, lng)
                        mapsFragment?.animateCameraToLocation(lat, lng)
                        Log.d(TAG, "Updating marker and animating camera for Angkot $id: Lat=$lat, Lng=$lng")
                        // Panggil endpoint /get-eta dengan tujuan berdasarkan status
                        trackAngkotViewModel.getETA(
                            driverLat = lat,
                            driverLong = lng,
                            pickupLat = startLat,
                            pickupLong = startLong,
                            destinationLat = destinationLat, // [Baru]
                            destinationLong = destinationLong // [Baru]
                        )
                    }
                }
                trackAngkotViewModel.updateAngkotPosition(id, lat, lng)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing angkot position message: ${e.message}")
            }
        }
        subscribedChannels.add(channel)
        Log.d(TAG, "Subscribed to angkot channel: $channelName")
    }

    private fun subscribeToOrderStatus(orderId: Int) {
        val channelName = "order.$orderId"
        val channel = pusher.subscribe(channelName)
        channel.bind("App\\Events\\OrderStatusUpdated") { event ->
            try {
                Log.d(TAG, "Received event on $channelName: ${event.data}")
                val data = JSONObject(event.data)
                val status = data.getString("status")
                if (isAdded) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Status pesanan: $status", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "Status pesanan diperbarui: $status")
                        if (status == "dijemput") {
                            binding.layoutCancelAngkotWait.hideCancelButton()
                            binding.tittleWaitingAngkot.text = "Menuju Tujuan"
                            // [Baru] Perbarui status pesanan dan panggil /get-eta
                            trackAngkotViewModel.updateOrderStatus("dijemput")
                            // Ambil posisi driver terbaru dari angkotPositions
                            val driverPosition = trackAngkotViewModel.angkotPositions.value?.get(driverId)
                            if (driverPosition != null) {
                                trackAngkotViewModel.getETA(
                                    driverLat = driverPosition.latitude,
                                    driverLong = driverPosition.longitude,
                                    pickupLat = startLat,
                                    pickupLong = startLong,
                                    destinationLat = destinationLat,
                                    destinationLong = destinationLong
                                )
                            } else {
                                Log.e(TAG, "Driver position not available for ETA on status dijemput")
                            }
                            // Animasi kamera ke polyline
                            val mapsFragment = childFragmentManager.findFragmentById(R.id.map_waiting_angkot) as? MapsFragment
                            if (mapsFragment != null && polyline.isNotEmpty()) {
                                try {
                                    val points = PolyUtil.decode(polyline)
                                    if (points.isNotEmpty()) {
                                        mapsFragment.animateCameraToBounds(points)
                                        Log.d(TAG, "Animating camera to polyline bounds on status dijemput")
                                    } else {
                                        Log.e(TAG, "No points decoded from polyline on status dijemput")
                                        Toast.makeText(requireContext(), "Gagal menampilkan rute", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error decoding polyline on status dijemput: ${e.message}")
                                    Toast.makeText(requireContext(), "Gagal menampilkan rute", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Log.e(TAG, "MapsFragment not found or polyline empty on status dijemput: polyline=$polyline")
                                Toast.makeText(requireContext(), "Gagal menampilkan rute", Toast.LENGTH_SHORT).show()
                            }
                        }
                        if (status == "selesai") {
                            navigateToMainActivity()
                        }
                        if (status == "dibatalkan") {
                            Toast.makeText(requireContext(), "Pesanan telah dibatalkan", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "Pesanan dibatalkan, kembali ke MapRouteFragment")
                            navigateToMainActivity()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing order status message: ${e.message}")
            }
        }
        subscribedChannels.add(channel)
        Log.d(TAG, "Subscribed to order channel: $channelName")
    }

    private fun observeOrderState() {
        trackAngkotViewModel.orderState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> {
                    showLoading(true)
                }
                is ResultState.Success -> {
                    val order = state.data.data
                    orderId = order?.orderId
                    order?.fullName?.let { binding.layoutCancelAngkotWait.setFullName(it) }
                    order?.platNomor?.let { binding.layoutCancelAngkotWait.setPlateNumber(it) }
                    orderId?.let {
                        subscribeToOrderStatus(it)
                        binding.layoutCancelAngkotWait.startCancelTimer()
                    }
                    // Inisialisasi marker awal dan animasi kamera
                    val mapsFragment = childFragmentManager.findFragmentById(R.id.map_waiting_angkot) as? MapsFragment
                    order?.lat?.let { lat ->
                        order.long?.let { lng ->
                            if (lat != 0.0 && lng != 0.0) {
                                mapsFragment?.updateAngkotMarker(driverId, lat, lng)
                                mapsFragment?.animateCameraToLocation(lat, lng)
                                Log.d(TAG, "Inisialisasi marker awal untuk Angkot $driverId: Lat=$lat, Lng=$lng")
                                // Panggil /get-eta saat pesanan dibuat
                                trackAngkotViewModel.getETA(
                                    driverLat = lat,
                                    driverLong = lng,
                                    pickupLat = startLat,
                                    pickupLong = startLong,
                                    destinationLat = destinationLat, // [Baru]
                                    destinationLong = destinationLong // [Baru]
                                )
                            } else {
                                mapsFragment?.animateCameraToLocation(startLat, startLong)
                                Log.d(TAG, "Posisi angkot tidak valid, animasi ke startLat=$startLat, startLong=$startLong")
                            }
                        }
                    } ?: run {
                        mapsFragment?.animateCameraToLocation(startLat, startLong)
                        Log.d(TAG, "Tidak ada posisi angkot, animasi ke startLat=$startLat, startLong=$startLong")
                    }
                    showLoading(false)
                    Log.d(TAG, "Pesanan dibuat: orderId=$orderId, fullName=${order?.fullName}, platNomor=${order?.platNomor}, lat=${order?.lat}, long=${order?.long}")
                }
                is ResultState.Error -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Gagal membuat pesanan: ${state.error}", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Error membuat pesanan: ${state.error}")
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container, MapRouteFragment())
                        .commit()
                }
            }
        }
    }

    private fun observeETAState() {
        trackAngkotViewModel.etaState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> {
                    Log.d(TAG, "Loading ETA")
                    // Tidak menampilkan loading untuk ETA agar UI tetap responsif
                }
                is ResultState.Success -> {
                    val eta = state.data.data?.eta ?: "N/A"
                    binding.layoutCancelAngkotWait.setETA(eta)
                    Log.d(TAG, "ETA updated: $eta")
                }
                is ResultState.Error -> {
                    binding.layoutCancelAngkotWait.setETA("N/A")
                    Log.e(TAG, "Error fetching ETA: ${state.error}")
                    Toast.makeText(requireContext(), "Gagal memperbarui ETA: ${state.error}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeCancelOrderState() {
        trackAngkotViewModel.cancelOrderState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> {
                    showLoading(true)
                    binding.layoutCancelAngkotWait.showCancelButton()
                    Log.d(TAG, "Canceling order, showing loading")
                }
                is ResultState.Success -> {
                    showLoading(false)
                    binding.layoutCancelAngkotWait.hideCancelButton()
                    Toast.makeText(requireContext(), "Pesanan berhasil dibatalkan", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Cancel order successful: message=${state.data.message}")
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container, MapRouteFragment())
                        .commit()
                }
                is ResultState.Error -> {
                    showLoading(false)
                    binding.layoutCancelAngkotWait.showCancelButton()
                    Toast.makeText(requireContext(), "Gagal membatalkan pesanan: ${state.error}", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Error canceling order: ${state.error}")
                }
            }
        }
    }

    private fun loadMaps() {
        val layoutPositionMaps = binding.root.findViewById<View>(R.id.map_waiting_angkot)
        if (layoutPositionMaps == null) {
            Log.e(TAG, "FrameLayout with ID map_waiting_angkot not found in layout")
            return
        }
        val existingFragment = childFragmentManager.findFragmentById(R.id.map_waiting_angkot)
        if (existingFragment == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.map_waiting_angkot, MapsFragment())
                .commit()
            childFragmentManager.executePendingTransactions()
            Log.d(TAG, "MapsFragment attached")
        } else {
            Log.d(TAG, "MapsFragment already exists")
        }

        if (polyline.isNotEmpty()) {
            Log.d(TAG, "Attempting to display polyline after map initialization: $polyline")
            val mapsFragment = childFragmentManager.findFragmentById(R.id.map_waiting_angkot) as? MapsFragment
            if (mapsFragment != null) {
                mapsFragment.mapReadyLiveData.observe(viewLifecycleOwner) { isReady ->
                    if (isReady) {
                        Log.d(TAG, "Map is ready, displaying polyline")
                        mapsFragment.displayPolyline(polyline)
                    } else {
                        Log.d(TAG, "Map not ready yet for polyline display")
                    }
                }
            } else {
                Log.e(TAG, "MapsFragment not found after initialization")
            }
        } else {
            Log.e(TAG, "Polyline empty: polyline=$polyline")
        }
    }

    override fun onLocationPermissionGranted() {
        trackAngkotViewModel.getUserLocation()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun navigateToMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        subscribedChannels.forEach { pusher.unsubscribe(it.name) }
        subscribedChannels.clear()
        pusher.disconnect()
        binding.layoutCancelAngkotWait.stopCancelTimer()
        Log.d(TAG, "Pusher disconnected and channels unsubscribed")
        _binding = null
    }

    companion object {
        fun newInstance(
            driverId: Int,
            startLat: Double,
            startLong: Double,
            destinationLat: Double,
            destinationLong: Double,
            numberOfPassengers: Int,
            totalPrice: Double,
            polyline: String = ""
        ): WaitingAngkotFragment {
            return WaitingAngkotFragment().apply {
                arguments = Bundle().apply {
                    putInt("driver_id", driverId)
                    putDouble("start_lat", startLat)
                    putDouble("start_long", startLong)
                    putDouble("destination_lat", destinationLat)
                    putDouble("destination_long", destinationLong)
                    putInt("number_of_passengers", numberOfPassengers)
                    putDouble("total_price", totalPrice)
                    putString("polyline", polyline)
                }
            }
        }
    }
}
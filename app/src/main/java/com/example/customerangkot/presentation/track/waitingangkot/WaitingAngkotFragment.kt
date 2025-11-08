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

    // [FIX] Pisahkan angkotId dan driverId
    private var driverId: Int = 0
    private var angkotId: Int = 0
    private var startLat: Double = 0.0
    private var startLong: Double = 0.0
    private var destinationLat: Double = 0.0
    private var destinationLong: Double = 0.0
    private var numberOfPassengers: Int = 0
    private var totalPrice: Double = 0.0
    private var orderId: Int? = null
    private var polyline: String = ""
    private var methodPayment: String = "tunai"

    private var platNomor: String? = null  // [BARU] Simpan plat nomor

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

        arguments?.let {
            driverId = it.getInt("driver_id", 0)
            angkotId = it.getInt("angkot_id", 0)
            startLat = it.getDouble("start_lat", 0.0)
            startLong = it.getDouble("start_long", 0.0)
            destinationLat = it.getDouble("destination_lat", 0.0)
            destinationLong = it.getDouble("destination_long", 0.0)
            numberOfPassengers = it.getInt("number_of_passengers", 0)
            totalPrice = it.getDouble("total_price", 0.0)
            polyline = it.getString("polyline", "")
            methodPayment = it.getString("method_payment", "tunai")
            Log.d(TAG, "Data diterima: driverId=$driverId, angkotId=$angkotId, platNomor=$platNomor")
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

        if (driverId == 0 || numberOfPassengers <= 0 || totalPrice <= 0.0) {
            Toast.makeText(requireContext(), "Data pesanan tidak valid", Toast.LENGTH_LONG).show()
            return
        }

        // [FIX] Gunakan driverId untuk create order
        trackAngkotViewModel.createOrder(
            driverId = driverId,
            startLat = startLat,
            startLong = startLong,
            destinationLat = destinationLat,
            destinationLong = destinationLong,
            numberOfPassengers = numberOfPassengers,
            totalPrice = totalPrice,
            methodPayment = methodPayment
        )

        cancelAction()

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)
    }

    private fun cancelAction() {
        binding.layoutCancelAngkotWait.setOnCancelClickListener {
            orderId?.let { id ->
                trackAngkotViewModel.cancelOrder(id)
            } ?: run {
                Toast.makeText(requireContext(), "Pesanan tidak valid untuk dibatalkan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initPusher() {
        val options = PusherOptions().setCluster("ap1")
        pusher = Pusher("d1373b327727bf1ce9cf", options)
        pusher.connection.bind(ConnectionState.ALL, object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                if (change.currentState == ConnectionState.DISCONNECTED) {
                    pusher.connect()
                }
            }

            override fun onError(message: String, code: String?, e: Exception?) {
                Log.e(TAG, "Pusher error: $message")
            }
        })
        pusher.connect()

        // [FIX] Gunakan angkotId untuk channel
        subscribeToAngkotPosition()
    }

    private fun subscribeToAngkotPosition() {
        val channelName = "angkot.$angkotId"
        val channel = pusher.subscribe(channelName)
        channel.bind("App\\Events\\AngkotLocationUpdated") { event ->
            try {
                val data = JSONObject(event.data)
                val id = data.getInt("id")  // angkotId
                val lat = data.getDouble("lat")
                val lng = data.getDouble("long")

                if (isAdded) {
                    requireActivity().runOnUiThread {
                        val mapsFragment = childFragmentManager.findFragmentById(R.id.map_waiting_angkot) as? MapsFragment
                        // [FIX] Kirim platNomor jika ada
                        mapsFragment?.updateAngkotMarker(id, lat, lng, platNomor)
                        mapsFragment?.animateCameraToLocation(lat, lng)

                        trackAngkotViewModel.getETA(
                            driverLat = lat,
                            driverLong = lng,
                            pickupLat = startLat,
                            pickupLong = startLong,
                            destinationLat = destinationLat,
                            destinationLong = destinationLong
                        )
                    }
                }
                trackAngkotViewModel.updateAngkotPosition(id, lat, lng)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing position: ${e.message}")
            }
        }
        subscribedChannels.add(channel)
    }

    private fun subscribeToOrderStatus(orderId: Int) {
        val channelName = "order.$orderId"
        val channel = pusher.subscribe(channelName)
        channel.bind("App\\Events\\OrderStatusUpdated") { event ->
            try {
                val data = JSONObject(event.data)
                val status = data.getString("status")
                if (isAdded) {
                    requireActivity().runOnUiThread {
                        if (status == "dijemput") {
                            binding.layoutCancelAngkotWait.hideCancelButton()
                            binding.tittleWaitingAngkot.text = "Menuju Tujuan"
                            trackAngkotViewModel.updateOrderStatus("dijemput")

                            val driverPosition = trackAngkotViewModel.angkotPositions.value?.get(angkotId)
                            if (driverPosition != null) {
                                trackAngkotViewModel.getETA(
                                    driverLat = driverPosition.latitude,
                                    driverLong = driverPosition.longitude,
                                    pickupLat = startLat,
                                    pickupLong = startLong,
                                    destinationLat = destinationLat,
                                    destinationLong = destinationLong
                                )
                            }

                            val mapsFragment = childFragmentManager.findFragmentById(R.id.map_waiting_angkot) as? MapsFragment
                            if (mapsFragment != null && polyline.isNotEmpty()) {
                                try {
                                    val points = PolyUtil.decode(polyline)
                                    if (points.isNotEmpty()) {
                                        mapsFragment.animateCameraToBounds(points)
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(requireContext(), "Gagal menampilkan rute", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        if (status == "selesai" || status == "dibatalkan") {
                            navigateToMainActivity()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing order status: ${e.message}")
            }
        }
        subscribedChannels.add(channel)
    }

    private fun observeOrderState() {
        trackAngkotViewModel.orderState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> showLoading(true)
                is ResultState.Success -> {
                    val order = state.data.data
                    orderId = order?.orderId
                    order?.fullName?.let { binding.layoutCancelAngkotWait.setFullName(it) }

                    // [BARU] Ambil platNomor dari response
                    platNomor = order?.platNomor
                    platNomor?.let { binding.layoutCancelAngkotWait.setPlateNumber(it) }

                    orderId?.let {
                        subscribeToOrderStatus(it)
                        binding.layoutCancelAngkotWait.startCancelTimer()
                    }

                    val mapsFragment = childFragmentManager.findFragmentById(R.id.map_waiting_angkot) as? MapsFragment
                    order?.lat?.let { lat ->
                        order.long?.let { lng ->
                            if (lat != 0.0 && lng != 0.0) {
                                // [FIX] Gunakan angkotId, kirim platNomor
                                mapsFragment?.updateAngkotMarker(angkotId, lat, lng, platNomor)
                                mapsFragment?.animateCameraToLocation(lat, lng)

                                trackAngkotViewModel.getETA(
                                    driverLat = lat,
                                    driverLong = lng,
                                    pickupLat = startLat,
                                    pickupLong = startLong,
                                    destinationLat = destinationLat,
                                    destinationLong = destinationLong
                                )
                            } else {
                                mapsFragment?.animateCameraToLocation(startLat, startLong)
                            }
                        }
                    } ?: run {
                        mapsFragment?.animateCameraToLocation(startLat, startLong)
                    }
                    showLoading(false)
                }
                is ResultState.Error -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Gagal membuat pesanan: ${state.error}", Toast.LENGTH_LONG).show()
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
                    // Tidak tampilkan loading agar tidak mengganggu UI
                    Log.d(TAG, "Loading ETA...")
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
                }
                is ResultState.Success -> {
                    showLoading(false)
                    binding.layoutCancelAngkotWait.hideCancelButton()
                    Toast.makeText(requireContext(), "Pesanan berhasil dibatalkan", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container, MapRouteFragment())
                        .commit()
                }
                is ResultState.Error -> {
                    showLoading(false)
                    binding.layoutCancelAngkotWait.showCancelButton()
                    Toast.makeText(requireContext(), "Gagal membatalkan: ${state.error}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun loadMaps() {
        val existingFragment = childFragmentManager.findFragmentById(R.id.map_waiting_angkot)
        if (existingFragment == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.map_waiting_angkot, MapsFragment())
                .commit()
            childFragmentManager.executePendingTransactions()
        }

        if (polyline.isNotEmpty()) {
            val mapsFragment = childFragmentManager.findFragmentById(R.id.map_waiting_angkot) as? MapsFragment
            mapsFragment?.mapReadyLiveData?.observe(viewLifecycleOwner) { isReady ->
                if (isReady) {
                    mapsFragment.displayPolyline(polyline)
                }
            }
        }
    }

    override fun onLocationPermissionGranted() {
        trackAngkotViewModel.getUserLocation()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun navigateToMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        subscribedChannels.forEach { pusher.unsubscribe(it.name) }
        subscribedChannels.clear()
        pusher.disconnect()
        binding.layoutCancelAngkotWait.stopCancelTimer()
        _binding = null
    }

    companion object {
        fun newInstance(
            driverId: Int,
            angkotId: Int,
            startLat: Double,
            startLong: Double,
            destinationLat: Double,
            destinationLong: Double,
            numberOfPassengers: Int,
            totalPrice: Double,
            polyline: String = "",
            methodPayment: String
        ): WaitingAngkotFragment {
            return WaitingAngkotFragment().apply {
                arguments = Bundle().apply {
                    putInt("driver_id", driverId)
                    putInt("angkot_id", angkotId)
                    putDouble("start_lat", startLat)
                    putDouble("start_long", startLong)
                    putDouble("destination_lat", destinationLat)
                    putDouble("destination_long", destinationLong)
                    putInt("number_of_passengers", numberOfPassengers)
                    putDouble("total_price", totalPrice)
                    putString("polyline", polyline)
                    putString("method_payment", methodPayment)
                }
            }
        }
    }
}
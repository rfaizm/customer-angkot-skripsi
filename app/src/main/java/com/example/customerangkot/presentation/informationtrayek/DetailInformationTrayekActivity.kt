package com.example.customerangkot.presentation.informationtrayek

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView // Ganti import ini
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.customerangkot.R
import com.example.customerangkot.databinding.ActivityDetailInformationTrayekBinding
import com.example.customerangkot.di.ResultState
import com.example.customerangkot.di.ViewModelFactory
import com.example.customerangkot.domain.entity.InformationItem
import com.example.customerangkot.domain.entity.TrayekItem
import com.example.customerangkot.presentation.adapter.InformationAdapter
import com.example.customerangkot.presentation.adapter.TrayekAdapter
import com.example.customerangkot.presentation.maps.MapsFragment
import com.example.customerangkot.utils.HorizontalSpaceItemDecoration
import com.example.customerangkot.utils.Utils.getTrayekList
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.Channel
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import org.json.JSONObject


class DetailInformationTrayekActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailInformationTrayekBinding
    private lateinit var trayekAdapter: TrayekAdapter
    private lateinit var pusher: Pusher
    private val subscribedChannels = mutableSetOf<Channel>()
    private var lastCameraUpdateTime = 0L

    private val viewModel by viewModels<DetailInformationTrayekViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityDetailInformationTrayekBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initPusher()
        val trayekList = getTrayekList(this)
        val longitude = intent.getDoubleExtra("LONG_USER", 0.0)
        val latitude = intent.getDoubleExtra("LAT_USER", 0.0)
        val trayekId = intent.getIntExtra("ID_TRAYEK", 0)

        loadMaps()
        setupSearching()
        setupRecycleView(trayekList, latitude, longitude, trayekId)
        observeAngkotState()
        observeAngkotPositions()

        if (trayekId != 0) {
            viewModel.getAngkotByTrayekId(latitude, longitude, trayekId)
        }
    }

    private fun initPusher() {
        val options = PusherOptions().setCluster("ap1")
        pusher = Pusher("d1373b327727bf1ce9cf", options)
        pusher.connection.bind(ConnectionState.ALL, object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                Log.d("DetailInformationTrayekActivity", "Pusher state changed from ${change.previousState} to ${change.currentState}")
                if (change.currentState == ConnectionState.DISCONNECTED) {
                    pusher.connect()
                    Log.d("DetailInformationTrayekActivity", "Attempting to reconnect Pusher")
                }
            }

            override fun onError(message: String, code: String?, e: Exception?) {
                Log.e("DetailInformationTrayekActivity", "Pusher connection error: $message, code: $code, exception: ${e?.message}")
            }
        })
        pusher.connect()
        Log.d("DetailInformationTrayekActivity", "Pusher initialized")
    }

    private fun subscribeToAngkotChannels(angkotIds: List<Int>) {
        subscribedChannels.forEach { pusher.unsubscribe(it.name) }
        subscribedChannels.clear()

        angkotIds.forEach { angkotId ->
            val channelName = "angkot.$angkotId"
            val channel = pusher.subscribe(channelName)
            channel.bind("App\\Events\\AngkotLocationUpdated") { event ->
                try {
                    Log.d("DetailInformationTrayekActivity", "Received event on $channelName: ${event.data}")
                    val data = JSONObject(event.data)
                    val id = data.getInt("id")
                    val lat = data.getDouble("lat")
                    val lng = data.getDouble("long")
                    Log.d("DetailInformationTrayekActivity", "Received position update: id=$id, lat=$lat, lng=$lng")

                    viewModel.updateAngkotPosition(id, lat, lng)
                } catch (e: Exception) {
                    Log.e("DetailInformationTrayekActivity", "Error parsing Pusher message: ${e.message}")
                }
            }
            subscribedChannels.add(channel)
            Log.d("DetailInformationTrayekActivity", "Subscribed to channel: $channelName")
        }
    }

    // Fungsi buat nampilin gambar list angkot online
    private fun showEmptyAngkot(isEmpty: Boolean) {
        binding.rvDetailAngkot.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.imageNoAngkot.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.textNoAngkot.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun setupSearching() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                trayekAdapter.filter(newText)
                return true
            }
        })
    }

    private fun setupRecycleView(trayekList: List<TrayekItem>, latitude: Double, longitude: Double, trayekIdFromIntent: Int) {
        trayekAdapter = TrayekAdapter(trayekList) { selectedTrayek ->
            val mapsFragment = supportFragmentManager.findFragmentById(R.id.map_detail_trayek) as? MapsFragment
            if (selectedTrayek != null) {
                // Baris diubah: Gunakan trayekId dari selectedTrayek, bukan trayekIdFromIntent
                viewModel.getAngkotByTrayekId(latitude, longitude, selectedTrayek.trayekId)
                mapsFragment?.clearAngkotMarkers()
                subscribedChannels.forEach { pusher.unsubscribe(it.name) }
                subscribedChannels.clear()
                Log.d("DetailInformationTrayekActivity", "Switching to trayek ${selectedTrayek.name} (ID: ${selectedTrayek.trayekId}), cleared markers and channels")
            } else {
                mapsFragment?.clearAngkotMarkers()
                subscribedChannels.forEach { pusher.unsubscribe(it.name) }
                subscribedChannels.clear()
                binding.rvDetailAngkot.adapter = null
                Log.d("DetailInformationTrayekActivity", "Deselected trayek, cleared markers, channels, and rvDetailAngkot")
            }
        }

        if (trayekIdFromIntent != 0) {
            val selectedIndex = trayekList.indexOfFirst { it.trayekId == trayekIdFromIntent }
            if (selectedIndex != -1) {
                trayekAdapter.setSelectedTrayekById(trayekIdFromIntent)
            }
        }

        binding.rvInformationTrayek.apply {
            layoutManager = LinearLayoutManager(this@DetailInformationTrayekActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = trayekAdapter
            val spacing = resources.getDimensionPixelSize(R.dimen.spacing_horizontal)
            addItemDecoration(HorizontalSpaceItemDecoration(spacing))
        }
    }

    private fun loadMaps() {
        val mapsFragment = MapsFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.map_detail_trayek, mapsFragment)
            .commit()
    }

    private fun observeAngkotState() {
        viewModel.angkotState.observe(this) { state ->
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
                    val mapsFragment = supportFragmentManager.findFragmentById(R.id.map_detail_trayek) as? MapsFragment
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
                                    mapsFragment?.animateCameraToLocation(selectedItem.latitude, selectedItem.longtitude)
                                }
                                is InformationItem.TrayekInformation -> {
                                    // Isi
                                }
                            }
                        }
                        binding.rvDetailAngkot.apply {
                            layoutManager = LinearLayoutManager(this@DetailInformationTrayekActivity)
                            adapter = informasiAdapter
                        }
                    }
                    binding.progressIndicator.visibility = View.GONE
                }
                is ResultState.Error -> {
                    binding.progressIndicator.visibility = View.GONE
                    showEmptyAngkot(true)
                    binding.rvDetailAngkot.adapter = null
                    val mapsFragment = supportFragmentManager.findFragmentById(R.id.map_detail_trayek) as? MapsFragment
                    mapsFragment?.clearAngkotMarkers()
                    subscribedChannels.forEach { pusher.unsubscribe(it.name) }
                    subscribedChannels.clear()
                    Log.d("DetailInformationTrayekActivity", "Error state, reset rvDetailAngkot and cleared markers/channels")
                }
            }
        }
    }

    private fun observeAngkotPositions() {
        Log.d("DetailInformationTrayekActivity", "Observing angkotPositions")
        viewModel.angkotPositions.observe(this) { positions ->
            val mapsFragment = supportFragmentManager.findFragmentById(R.id.map_detail_trayek) as? MapsFragment
            positions.forEach { (angkotId, latLng) ->
                mapsFragment?.updateAngkotMarker(angkotId, latLng.latitude, latLng.longitude)
                if (System.currentTimeMillis() - lastCameraUpdateTime > 2000) {
                    mapsFragment?.animateCameraToLocation(latLng.latitude, latLng.longitude)
                    Log.d("DetailInformationTrayekActivity", "Camera animated to Angkot $angkotId: Lat=${latLng.latitude}, Lng=${latLng.longitude}")
                    lastCameraUpdateTime = System.currentTimeMillis()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        subscribedChannels.forEach { pusher.unsubscribe(it.name) }
        pusher.disconnect()
        Log.d("DetailInformationTrayekActivity", "Pusher disconnected")
    }
}
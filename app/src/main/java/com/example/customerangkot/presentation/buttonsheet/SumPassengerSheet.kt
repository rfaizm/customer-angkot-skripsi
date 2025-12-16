package com.example.customerangkot.presentation.buttonsheet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.customerangkot.MainActivity
import com.example.customerangkot.R
import com.example.customerangkot.databinding.ItemSumPassengersBinding
import com.example.customerangkot.di.ResultState
import com.example.customerangkot.di.ViewModelFactory
import com.example.customerangkot.presentation.track.TrackAngkotViewModel
import com.example.customerangkot.presentation.track.waitingangkot.WaitingAngkotFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class SumPassengerSheet : BottomSheetDialogFragment() {

    private lateinit var binding: ItemSumPassengersBinding
    private val trackAngkotViewModel by viewModels<TrackAngkotViewModel> { ViewModelFactory.getInstance(requireContext()) }

    private var angkotId: Int? = null

    private var driverId: Int? = null
    private var startLat: Double = 0.0
    private var startLong: Double = 0.0
    private var destinationLat: Double = 0.0
    private var destinationLong: Double = 0.0
    private var price: Double = 0.0
    private var polyline: String = ""
    private val TAG = "SumPassengerSheet"

    private var tempOrderData: TempOrderData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            angkotId = it.getInt("angkot_id", -1).takeIf { it != -1 }
            startLat = it.getDouble("start_lat", 0.0)
            startLong = it.getDouble("start_long", 0.0)
            destinationLat = it.getDouble("destination_lat", 0.0)
            destinationLong = it.getDouble("destination_long", 0.0)
            price = it.getDouble("price", 0.0)
            polyline = it.getString("polyline", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = ItemSumPassengersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.radioPaymentCash.isChecked = true
        binding.buttonSave.setOnClickListener { saveButton() }


        observePusherConnectionState()
        observeDriverIdState()
        observeSaldoState()
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    private fun observePusherConnectionState() {
        // Amati status koneksi Pusher
        trackAngkotViewModel.pusherConnectionState.observe(viewLifecycleOwner) { state ->
            Log.d(TAG, "observePusherConnectionState: state received -> $state")
            when (state) {
                is ResultState.Loading -> showLoading(true)
                is ResultState.Success -> {
                    showLoading(false)
                    if (state.data) {
                        // Pusher OK → lanjut ke WaitingAngkotFragment
                        Log.d(
                            TAG,
                            "angkotId: $angkotId, " +
                                    "startLat: $startLat, " +
                                    "startLong: $startLong, " +
                                    "destinationLat: $destinationLat, " +
                                    "destinationLong: $destinationLong, " +
                                    "numberOfPassengers: ${tempOrderData?.numberOfPassengers}, " +
                                    "totalPrice: ${tempOrderData?.totalPrice}, " +
                                    "polyline: $polyline, " +
                                    "methodPayment: ${tempOrderData?.methodPayment}"
                        )
                        proceedToWaitingAngkot()
                    } else {
                        // Pusher gagal → tampilkan dialog "Server sibuk"
                        showServerBusyDialog()
                    }
                }
                is ResultState.Error -> {
                    showLoading(false)
                    showServerBusyDialog(state.error)
                }
            }
        }
    }

    private fun observeDriverIdState() {
        trackAngkotViewModel.driverIdState.observe(viewLifecycleOwner) { state ->
            Log.d(TAG, "observeDriverIdState: state received -> $state")
            when (state) {
                is ResultState.Loading -> showLoading(true)
                is ResultState.Success -> {
                    showLoading(false)
                    driverId = state.data.data?.driverId
                    Log.d(TAG, "observeDriverIdState: Driver ID diterima: $driverId")
                    Log.d(TAG, "observeDriverIdState: Triggering checkPusherConnection()")
                    trackAngkotViewModel.checkPusherConnection()  // Lanjut ke Pusher
                }
                is ResultState.Error -> {
                    showLoading(false)
                    showServerBusyDialog(state.error)
                }
            }
        }
    }


    private fun saveButton() {
        val inputText = binding.inputSumPassengers.text?.toString()?.trim()
        if (inputText.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Masukkan jumlah penumpang", Toast.LENGTH_SHORT).show()
            return
        }

        val numberOfPassengers = inputText.toIntOrNull()
        if (numberOfPassengers == null || numberOfPassengers <= 0) {
            Toast.makeText(requireContext(), "Jumlah penumpang harus angka positif", Toast.LENGTH_SHORT).show()
            return
        }

        // [BARU] Validasi maksimal 14 penumpang
        if (numberOfPassengers > 14) {
            Toast.makeText(requireContext(), "Kapasitas angkot maksimal 14 penumpang", Toast.LENGTH_LONG).show()
            return
        }

        if (angkotId == null) {
            Toast.makeText(requireContext(), "Pilih angkot terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val methodPayment = when {
            binding.radioPaymentCash.isChecked -> "tunai"
            binding.radioPaymentBalance.isChecked -> "saldo"
            else -> "tunai"
        }

        val totalPrice = if (numberOfPassengers >= 2) price * numberOfPassengers else price
        tempOrderData = TempOrderData(numberOfPassengers, totalPrice, methodPayment)

        // [BARU] Cek saldo jika pakai saldo
        if (methodPayment == "saldo") {
            Log.d(TAG, "saveButton: Payment method is 'saldo', triggering getSaldo()")
            trackAngkotViewModel.getSaldo()
            return  // Tunggu saldo selesai
        }

        // Lanjut ke driver
        getDriverAndProceed()
    }

    private fun getDriverAndProceed() {
        angkotId?.let { id ->
            showLoading(true)
            Log.d(TAG, "getDriverAndProceed: Triggering getDriverWithAngkotId with angkotId: $id")
            trackAngkotViewModel.getDriverWithAngkotId(id)
        }
    }

    private fun observeSaldoState() {
        trackAngkotViewModel.saldoState.observe(viewLifecycleOwner) { state ->
            Log.d(TAG, "observeSaldoState: state received -> $state")
            when (state) {
                is ResultState.Loading -> showLoading(true)
                is ResultState.Success -> {
                    showLoading(false)
                    val saldo = state.data.data?.saldo ?: 0.0
                    val totalPrice: Double = tempOrderData?.totalPrice ?: 0.0

                    if (saldo.toDouble() >= totalPrice) {
                        Log.d(TAG, "observeSaldoState: Saldo cukup (Rp $saldo >= Rp $totalPrice), proceeding to get driver.")
                        // Saldo cukup → lanjut ke driver
                        getDriverAndProceed()
                    } else {
                        Toast.makeText(requireContext(), "Saldo tidak cukup. Saldo Anda: Rp ${saldo}", Toast.LENGTH_LONG).show()
                    }
                }
                is ResultState.Error -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Gagal cek saldo: ${state.error}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    private fun proceedToWaitingAngkot() {
        tempOrderData?.let { data ->
            if (driverId == null) {
                Toast.makeText(requireContext(), "Gagal mendapatkan driver", Toast.LENGTH_SHORT).show()
                return
            }

            if (angkotId == null) {
                Toast.makeText(requireContext(), "Gagal mendapatkan angkot", Toast.LENGTH_SHORT).show()
                return
            }

            val waitingFragment = WaitingAngkotFragment.newInstance(
                driverId = driverId!!,  // Sekarang aman karena sudah dicek
                angkotId = angkotId!!,
                startLat = startLat,
                startLong = startLong,
                destinationLat = destinationLat,
                destinationLong = destinationLong,
                numberOfPassengers = data.numberOfPassengers,
                totalPrice = data.totalPrice,
                polyline = polyline,
                methodPayment = data.methodPayment,
                isFromActiveOrder = false
            )

            parentFragmentManager.beginTransaction()
                .replace(R.id.container, waitingFragment)
                .addToBackStack("ChooseAngkotFragment")
                .commit()

            dismiss()
        }
    }

    // Dialog: Server sedang sibuk
    private fun showServerBusyDialog(errorMessage: String? = null) {
        val message = errorMessage ?: "Server sedang sibuk. Silakan coba lagi nanti."
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Server Sibuk")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                navigateToMainActivity()
            }
            .setCancelable(false)
            .show()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(requireActivity(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private data class TempOrderData(
        val numberOfPassengers: Int,
        val totalPrice: Double,
        val methodPayment: String
    )

    companion object {
        fun newInstance(
            angkotId: Int?,
            startLat: Double,
            startLong: Double,
            destinationLat: Double,
            destinationLong: Double,
            price: Double,
            polyline: String = ""
        ): SumPassengerSheet = SumPassengerSheet().apply {
            arguments = Bundle().apply {
                angkotId?.let { putInt("angkot_id", it) }
                putDouble("start_lat", startLat)
                putDouble("start_long", startLong)
                putDouble("destination_lat", destinationLat)
                putDouble("destination_long", destinationLong)
                putDouble("price", price)
                putString("polyline", polyline)
            }
        }
    }
}
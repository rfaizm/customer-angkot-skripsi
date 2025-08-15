package com.example.customerangkot.presentation.buttonsheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.customerangkot.R
import com.example.customerangkot.databinding.ItemSumPassengersBinding
import com.example.customerangkot.presentation.track.waitingangkot.WaitingAngkotFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class SumPassengerSheet : BottomSheetDialogFragment() {

    private lateinit var binding: ItemSumPassengersBinding

    // Atribut untuk menyimpan data dari ChooseAngkotFragment
    private var driverId: Int? = null
    private var startLat: Double = 0.0
    private var startLong: Double = 0.0
    private var destinationLat: Double = 0.0
    private var destinationLong: Double = 0.0
    private var price: Double = 0.0
    private var polyline: String = "" // [Baru] Atribut untuk polyline

    private val TAG = "SumPassengerSheet" // [Baru] Tag untuk logging

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // [Berubah] Ambil data polyline dari arguments
        arguments?.let {
            driverId = it.getInt("driver_id", -1).takeIf { it != -1 }
            startLat = it.getDouble("start_lat", 0.0)
            startLong = it.getDouble("start_long", 0.0)
            destinationLat = it.getDouble("destination_lat", 0.0)
            destinationLong = it.getDouble("destination_long", 0.0)
            price = it.getDouble("price", 0.0)
            polyline = it.getString("polyline", "") // [Baru]
            Log.d(TAG, "Data diterima: driverId=$driverId, startLat=$startLat, startLong=$startLong, destinationLat=$destinationLat, destinationLong=$destinationLong, price=$price, polyline=$polyline")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ItemSumPassengersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSave.setOnClickListener {
            saveButton()
        }
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    private fun saveButton() {
        // Validasi input jumlah penumpang
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

        // Validasi driverId
        if (driverId == null) {
            Toast.makeText(requireContext(), "Pilih angkot terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        // Hitung total harga
        val totalPrice = if (numberOfPassengers >= 2) {
            price * numberOfPassengers
        } else {
            price
        }

        Log.d(TAG, "Data untuk WaitingAngkotFragment: driverId=$driverId, startLat=$startLat, startLong=$startLong, destinationLat=$destinationLat, destinationLong=$destinationLong, numberOfPassengers=$numberOfPassengers, totalPrice=$totalPrice, polyline=$polyline")

        // [Berubah] Navigasi ke WaitingAngkotFragment dengan data polyline
        val waitingAngkotFragment = WaitingAngkotFragment.newInstance(
            driverId = driverId!!,
            startLat = startLat,
            startLong = startLong,
            destinationLat = destinationLat,
            destinationLong = destinationLong,
            numberOfPassengers = numberOfPassengers,
            totalPrice = totalPrice,
            polyline = polyline // [Baru]
        )
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, waitingAngkotFragment)
            .addToBackStack("ChooseAngkotFragment")
            .commit()
        dismiss()
    }

    companion object {
        // [Berubah] Tambahkan parameter polyline
        fun newInstance(
            driverId: Int?,
            startLat: Double,
            startLong: Double,
            destinationLat: Double,
            destinationLong: Double,
            price: Double,
            polyline: String = "" // [Baru]
        ): SumPassengerSheet {
            return SumPassengerSheet().apply {
                arguments = Bundle().apply {
                    if (driverId != null) {
                        putInt("driver_id", driverId)
                    }
                    putDouble("start_lat", startLat)
                    putDouble("start_long", startLong)
                    putDouble("destination_lat", destinationLat)
                    putDouble("destination_long", destinationLong)
                    putDouble("price", price)
                    putString("polyline", polyline) // [Baru]
                }
            }
        }
    }
}
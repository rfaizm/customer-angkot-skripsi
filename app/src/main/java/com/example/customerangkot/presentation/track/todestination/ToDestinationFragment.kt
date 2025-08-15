package com.example.customerangkot.presentation.track.todestination

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.example.customerangkot.R
import com.example.customerangkot.databinding.FragmentToDestinationBinding
import com.example.customerangkot.di.ViewModelFactory
import com.example.customerangkot.presentation.maps.MapsFragment
import com.example.customerangkot.presentation.track.TrackAngkotViewModel
import com.example.customerangkot.utils.LocationPermissionListener


class ToDestinationFragment : Fragment(), LocationPermissionListener {

    private var _binding : FragmentToDestinationBinding? = null

    private val binding get() = _binding!!

    private val trackAngkotViewModel by viewModels<TrackAngkotViewModel> {
        ViewModelFactory.getInstance(requireContext())
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentToDestinationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadMaps()

        binding.layoutCancelAngkotToDestination.hideCancelButton()

        // Tambahkan callback untuk menangani tombol back
        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Kosongkan ini untuk memblokir tombol back
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, // Lifecycle owner
            backCallback
        )
    }

    private fun loadMaps() {
        val layoutPositionMaps = binding.root.findViewById<View>(R.id.map_to_destination_angkot)
        if (layoutPositionMaps == null) {
            Log.e("AngkotFragment", "FrameLayout with ID position_maps not found in layout")
            return
        }
        val existingFragment = childFragmentManager.findFragmentById(R.id.map_to_destination_angkot)
        if (existingFragment == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.map_to_destination_angkot, MapsFragment())
                .commit()
            Log.d("AngkotFragment", "MapsFragment attached")
        } else {
            Log.d("AngkotFragment", "MapsFragment already exists")
        }
    }

    override fun onLocationPermissionGranted() {
        trackAngkotViewModel.getUserLocation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ToDestinationFragment()
    }
}
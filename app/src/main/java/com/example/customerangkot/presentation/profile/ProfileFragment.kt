package com.example.customerangkot.presentation.profile


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.customerangkot.databinding.FragmentProfileBinding
import com.example.customerangkot.di.ResultState
import com.example.customerangkot.di.ViewModelFactory
import com.example.customerangkot.presentation.adapter.HistoryAdapter
import com.example.customerangkot.presentation.login.LoginActivity
import com.example.customerangkot.presentation.profile.detail.DetailProfileActivity
import com.example.customerangkot.utils.DataHistory
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var fullName : String? = null
    private var email : String? = null
    private var phoneNumber : String? = null

    private val profileViewModel by viewModels<ProfileViewModel> {
        ViewModelFactory.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeHistoryState()
        observeLogoutState()
        observeProfileState()

        binding.fullname.setOnClickListener {
            val intent = Intent(requireContext(), DetailProfileActivity::class.java)
            intent.putExtra(DetailProfileActivity.EXTRA_FULL_NAME, fullName)
            intent.putExtra(DetailProfileActivity.EXTRA_EMAIL, email)
            intent.putExtra(DetailProfileActivity.EXTRA_PHONE_NUMBER, phoneNumber)
            startActivity(intent)
        }


        binding.logoutButton.setOnClickListener {
            profileViewModel.logout()
        }

        // Panggil endpoint /history saat fragment dimuat
        profileViewModel.getHistory()

        // Panggil endpoint /profile saat fragment dimuat
        profileViewModel.getProfile()
    }

    private fun setupRecyclerView() {
        binding.rvHistory.layoutManager = LinearLayoutManager(context)
        // Inisialisasi adapter kosong untuk menghindari null
        binding.rvHistory.adapter = HistoryAdapter(emptyList())
    }

    private fun observeHistoryState() {
        profileViewModel.historyState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> {
                    showLoading(true)
                }
                is ResultState.Success -> {
                    showLoading(false)
                    val historyItems = state.data.data?.filterNotNull() ?: emptyList()
                    binding.rvHistory.adapter = HistoryAdapter(historyItems)
                    if (historyItems.isEmpty()) {
                        Toast.makeText(requireContext(), "Tidak ada riwayat pesanan", Toast.LENGTH_SHORT).show()
                    }
                }
                is ResultState.Error -> {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Gagal memuat riwayat: ${state.error}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun observeLogoutState() {
        profileViewModel.logoutState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> {
                    binding.logoutButton.isEnabled = false
                    showLoading(true)
                }
                is ResultState.Success -> {
                    binding.logoutButton.isEnabled = true
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Berhasil!")
                        .setMessage("Anda telah logout.")
                        .setPositiveButton("OK") { _, _ ->
                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            requireActivity().finish()
                        }
                        .show()
                    showLoading(false)
                }
                is ResultState.Error -> {
                    binding.logoutButton.isEnabled = true
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                    showLoading(false)
                }
            }
        }
    }

    private fun observeProfileState() {
        profileViewModel.getProfile.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> {
                    showLoading(true)
                }
                is ResultState.Success -> {
                    showLoading(false)
                    fullName = state.data.data?.fullName ?: "N/A"
                    email = state.data.data?.email ?: "N/A"
                    phoneNumber = state.data.data?.noHp ?: "N/A"
                    binding.fullname.text = fullName
                    binding.gmailText.text = email
                }
                is ResultState.Error -> {
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                    showLoading(false)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
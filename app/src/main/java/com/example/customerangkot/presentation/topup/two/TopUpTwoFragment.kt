package com.example.customerangkot.presentation.topup.two

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.customerangkot.MainActivity
import com.example.customerangkot.R
import com.example.customerangkot.databinding.FragmentTopUpTwoBinding


class TopUpTwoFragment : Fragment() {
    private var _binding: FragmentTopUpTwoBinding? = null
    private val binding get() = _binding!!

    // Tambahkan variabel untuk handle delay
    private val navigateRunnable = Runnable { navigateToMainActivity() }
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentTopUpTwoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(this)
            .load(R.drawable.success_payment)
            .into(binding.gifImage)


        handler.postDelayed(navigateRunnable, 3000)
    }

    private fun navigateToMainActivity() {
        // Pindah ke MainActivity dan clear back stack
        val intent = Intent(requireActivity(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        requireActivity().finish()  // Tutup activity saat ini
    }

    override fun onDestroyView() {
        handler.removeCallbacks(navigateRunnable)
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = TopUpTwoFragment()
    }
}
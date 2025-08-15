package com.example.customerangkot.presentation.topup.one

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.customerangkot.R
import com.example.customerangkot.databinding.FragmentTopUpOneBinding
import com.example.customerangkot.di.ResultState
import com.example.customerangkot.di.ViewModelFactory
import com.example.customerangkot.presentation.topup.TopUpViewModel
import com.example.customerangkot.presentation.topup.two.TopUpTwoFragment
import com.example.customerangkot.utils.RadioButtonUtils
import com.example.customerangkot.utils.Utils

class TopUpOneFragment : Fragment() {

    private var _binding: FragmentTopUpOneBinding? = null
    private val binding get() = _binding!!
    private var isUpdatingFromRadio = false

    private val topUpViewModel by viewModels<TopUpViewModel> {
        ViewModelFactory.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopUpOneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRadioButtons()
        setupInputTextListener()
        setupButtonConfirmation()
        observeTopUpState()
    }

    private fun setupRadioButtons() {
        val rightGroup = binding.radioGroupTopupRight
        val leftGroup = binding.radioGroupTopupLeft

        RadioButtonUtils.manageMultipleRadioGroups(rightGroup, leftGroup)

        RadioButtonUtils.setupRadioButtonTextListener(rightGroup, leftGroup) { selectedText ->
            isUpdatingFromRadio = true
            // Mengubah format "Rp. 50.000" menjadi "50000"
            val cleanedText = selectedText
                .replace("Rp. ", "")
                .replace(".", "")
            binding.inputTopupInputText.setText(cleanedText)
            binding.buttonConfirmation.isEnabled = true // Aktifkan tombol saat radio dipilih
            isUpdatingFromRadio = false
        }
    }

    private fun setupInputTextListener() {
        binding.inputTopupInputText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (!isUpdatingFromRadio) {
                    // Reset radio buttons jika input diubah manual
                    RadioButtonUtils.getAllRadioButtons(
                        binding.radioGroupTopupRight,
                        binding.radioGroupTopupLeft
                    ).forEach { it.isChecked = false }
                }
                // Aktifkan/nonaktifkan tombol berdasarkan input
                binding.buttonConfirmation.isEnabled = !s.isNullOrEmpty() && s.toString().trim().isNotBlank()
            }
        })
    }

    private fun setupButtonConfirmation() {
        // Nonaktifkan tombol secara default
        binding.buttonConfirmation.isEnabled = false

        binding.buttonConfirmation.setOnClickListener {
            val inputText = binding.inputTopupInputText.text.toString().trim()
            val nominal = inputText.toIntOrNull()

            if (nominal == null || nominal <= 0) {
                Toast.makeText(requireContext(), "Masukkan nominal top-up yang valid", Toast.LENGTH_SHORT).show()
                Log.e("TopUpOneFragment", "Nominal tidak valid: $inputText")
                return@setOnClickListener
            }

            // Panggil dialog konfirmasi
            Utils.showConfirmationDialog(
                context = requireContext(),
                navigateToNextFragment = { topUp(nominal) },
                textTitle = "Konfirmasi Top Up",
                textMessage = "Apakah Anda yakin ingin melanjutkan top up sebesar ${Utils.formatNumber(nominal)}?",
                textPositive = "Ya",
                textNegative = "Tidak"
            )
        }
    }

    private fun topUp(nominal: Int) {
        topUpViewModel.topUp(nominal)
    }

    private fun observeTopUpState() {
        topUpViewModel.topUpState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> {
                    showLoading(true)
                }
                is ResultState.Success -> {
                    showLoading(false)
                    Toast.makeText(context, "Top-up berhasil: ${state.data.message}", Toast.LENGTH_SHORT).show()
                    Log.d("TopUpOneFragment", "Top-up berhasil: ${state.data.message}, saldo=${state.data.data?.saldo}")
                    navigateToNextFragment()
                }
                is ResultState.Error -> {
                    showLoading(false)
                    Toast.makeText(context, "Gagal top-up: ${state.error}", Toast.LENGTH_LONG).show()
                    Log.e("TopUpOneFragment", "Error top-up: ${state.error}")
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun navigateToNextFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, TopUpTwoFragment.newInstance())
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = TopUpOneFragment()
    }
}

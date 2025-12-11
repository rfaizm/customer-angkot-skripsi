package com.example.customerangkot.presentation.card

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.customerangkot.databinding.LayoutCancelAngkotBinding

class LayoutCancelAngkot @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding = LayoutCancelAngkotBinding.inflate(LayoutInflater.from(context), this, true)
    private var cancelTimer: CountDownTimer? = null
    private var onCancelClickListener: (() -> Unit)? = null
    private val TAG = "LayoutCancelAngkot"

    init {
        binding.buttonSave.visibility = View.VISIBLE

        // [BARU] Click listener: cek apakah enabled
        binding.buttonSave.setOnClickListener {
            if (binding.buttonSave.isEnabled) {
                Log.d(TAG, "Cancel button clicked (enabled)")
                onCancelClickListener?.invoke()
            } else {
                Log.d(TAG, "Cancel button clicked but disabled")
                Toast.makeText(context, "Pesanan tidak bisa dibatalkan. Pastikan Anda menyelesaikan pesanan.", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun setFullName(name: String) {
        binding.fullNameDriver.text = name
    }

    fun setPlateNumber(plateNumber: String) {
        binding.plateNumber.text = plateNumber
    }

    fun hideCancelButton() {
        binding.buttonSave.isEnabled = false
        binding.cancelProgressBar.visibility = View.GONE
        Log.d(TAG, "Cancel button disabled and progress bar hidden")
    }

    fun showCancelButton() {
        binding.buttonSave.visibility = View.VISIBLE
        binding.buttonSave.isEnabled = true
        binding.cancelProgressBar.visibility = View.VISIBLE
        Log.d(TAG, "Cancel button enabled and progress bar shown")
    }

    // [Baru] Mulai timer 30 detik untuk pembatalan
    fun startCancelTimer() {
        binding.cancelProgressBar.visibility = View.VISIBLE
        binding.buttonSave.isEnabled = true
        cancelTimer?.cancel()
        cancelTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000).toInt()
                binding.cancelProgressBar.progress = secondsRemaining
                Log.d(TAG, "Cancel timer: $secondsRemaining seconds remaining")
            }

            override fun onFinish() {
                binding.buttonSave.isEnabled = false
                binding.cancelProgressBar.visibility = View.GONE
                Log.d(TAG, "Cancel timer finished, button disabled")
            }
        }.start()
    }

    // [Baru] Set listener untuk tombol batal
    fun setOnCancelClickListener(listener: () -> Unit) {
        this.onCancelClickListener = listener
    }

    // [Baru] Hentikan timer saat view dihancurkan
    fun stopCancelTimer() {
        cancelTimer?.cancel()
        Log.d(TAG, "Cancel timer stopped")
    }

    fun setETA(eta : String) {
        binding.etaMnt.text = eta
    }
}
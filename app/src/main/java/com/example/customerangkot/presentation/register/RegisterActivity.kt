package com.example.customerangkot.presentation.register

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.customerangkot.R
import com.example.customerangkot.databinding.ActivityRegisterBinding
import com.example.customerangkot.di.ResultState
import com.example.customerangkot.di.ViewModelFactory
import com.example.customerangkot.presentation.login.LoginActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel by viewModels<RegisterViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupActions()
        observeRegisterState()
    }

    private fun setupActions() {
        binding.buttonSignup.setOnClickListener {
            val name = binding.inputFullnameInputText.text.toString().trim()
            val email = binding.inputEmailInputText.text.toString().trim()
            val noHp = binding.inputNoHPInputText.text.toString().trim()
            val password = binding.inputPasswordInputText.text.toString().trim()
            viewModel.register(name, email, noHp, password)
        }

        binding.signUp.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeRegisterState() {
        viewModel.registerState.observe(this) { state ->
            when (state) {
                is ResultState.Loading -> {
                    binding.buttonSignup.isEnabled = false
                    // Tambahkan loading indicator jika diperlukan
                    showLoading(true)
                }
                is ResultState.Success -> {
                    binding.buttonSignup.isEnabled = true
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Berhasil!")
                        .setMessage("Registrasi berhasil. Silakan login.")
                        .setPositiveButton("OK") { _, _ ->
                            val intent = Intent(this, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
                        }
                        .show()
                    showLoading(false)
                }
                is ResultState.Error -> {
                    binding.buttonSignup.isEnabled = true
                    Toast.makeText(this, state.error, Toast.LENGTH_LONG).show()
                    showLoading(false)
                }
            }
        }
    }


    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
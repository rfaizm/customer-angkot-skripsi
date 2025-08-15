package com.example.customerangkot.presentation.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.customerangkot.MainActivity
import com.example.customerangkot.R
import com.example.customerangkot.databinding.ActivityLoginBinding
import com.example.customerangkot.di.ResultState
import com.example.customerangkot.di.ViewModelFactory
import com.example.customerangkot.presentation.register.RegisterActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private val viewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupActions()
        observeLoginState()
    }

    private fun setupActions() {
        binding.buttonSave.setOnClickListener {
            val email = binding.inputEmailInputText.text.toString().trim()
            val password = binding.inputPasswordInputText.text.toString().trim()
            if (validateInput(email, password)) {
                viewModel.login(email, password)
            }
        }

        binding.signUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true
        if (email.isBlank() || !email.contains("@")) {
            binding.inputEmailTextLayout.error = "Email tidak valid"
            isValid = false
        } else {
            binding.inputEmailTextLayout.error = null
        }
        if (password.length < 8) {
            binding.inputPasswordTextLayout.error = "Password minimal 8 karakter"
            isValid = false
        } else {
            binding.inputPasswordTextLayout.error = null
        }
        return isValid
    }

    private fun observeLoginState() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is ResultState.Loading -> {
                    showLoading(true)
                }
                is ResultState.Success -> {
                    showLoading(false)
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Berhasil!")
                        .setMessage("Login berhasil.")
                        .setPositiveButton("OK") { _, _ ->
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
                        }
                        .show()
                }
                is ResultState.Error -> {
                    showLoading(false)
                    Toast.makeText(this, state.error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }




    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
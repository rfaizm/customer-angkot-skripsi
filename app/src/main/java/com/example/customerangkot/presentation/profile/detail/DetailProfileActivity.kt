package com.example.customerangkot.presentation.profile.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.customerangkot.R
import com.example.customerangkot.databinding.ActivityDetailProfileBinding

class DetailProfileActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDetailProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityDetailProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val fullName = intent.getStringExtra(EXTRA_FULL_NAME)
        val email = intent.getStringExtra(EXTRA_EMAIL)
        val phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER)

        Glide.with(this)
            .load("https://picsum.photos/seed/image2/400/300")
            .into(binding.detailProfilePicture)

        binding.inputFullnameInputText.setText(fullName)
        binding.inputEmailInputText.setText(email)
        binding.inputNumberInputText.setText(phoneNumber)
    }

    companion object {
        const val EXTRA_FULL_NAME = "extra_full_name"
        const val EXTRA_EMAIL = "extra_email"
        const val EXTRA_PHONE_NUMBER = "extra_phone_number"
    }
}
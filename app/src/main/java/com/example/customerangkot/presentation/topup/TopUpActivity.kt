package com.example.customerangkot.presentation.topup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.customerangkot.R
import com.example.customerangkot.databinding.ActivityTopUpBinding
import com.example.customerangkot.presentation.topup.one.TopUpOneFragment

class TopUpActivity : AppCompatActivity() {

    private lateinit var binding : ActivityTopUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, TopUpOneFragment.newInstance())
                .commitNow()
        }
    }
}
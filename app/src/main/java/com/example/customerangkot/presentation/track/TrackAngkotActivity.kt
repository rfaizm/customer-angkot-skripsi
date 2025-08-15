package com.example.customerangkot.presentation.track

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.customerangkot.R
import com.example.customerangkot.databinding.ActivityTrackAngkotBinding
import com.example.customerangkot.presentation.track.map.MapRouteFragment

class TrackAngkotActivity : AppCompatActivity() {
    private lateinit var binding : ActivityTrackAngkotBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    finish()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityTrackAngkotBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MapRouteFragment.newInstance())
                .commitNow()
        }
    }
}
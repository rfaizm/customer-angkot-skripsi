package com.example.customerangkot

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.customerangkot.data.preference.UserPreference
import com.example.customerangkot.data.preference.dataStore
import com.example.customerangkot.databinding.ActivityMainBinding
import com.example.customerangkot.presentation.login.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var userPreference: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi UserPreference
        userPreference = UserPreference.getInstance(dataStore)

        // Periksa status login sebelum mengatur UI
        checkLoginStatus()

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_angkot, R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        supportActionBar?.hide()
    }

    private fun checkLoginStatus() {
        // Periksa IS_LOGIN_KEY menggunakan UserPreference
        val isLoggedIn = userPreference.getLogin() ?: false
        if (!isLoggedIn) {
            // Jika belum login, arahkan ke LoginActivity dan bersihkan tumpukan aktivitas
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish() // Tutup MainActivity
        }
    }
}
package com.example.kohistanbookingapp.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.kohistanbookingapp.R
import com.example.kohistanbookingapp.databinding.ActivitySplashBinding
import com.example.kohistanbookingapp.util.SharedPreferencesHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        startSplashAnimations()
    }

    private fun startSplashAnimations() {
        val logoAnim = AnimationUtils.loadAnimation(this, R.anim.slide_in_top)
        binding.ivLogo.startAnimation(logoAnim)

        lifecycleScope.launch {
            delay(150.milliseconds)
            val kohistanText = "KOHISTAN"
            val kohistanStringBuilder = StringBuilder()
            for (char in kohistanText) {
                kohistanStringBuilder.append(char)
                binding.tvKohistan.text = kohistanStringBuilder.toString()
                delay(100.milliseconds)
            }

            val bookingText = "BOOKING"
            val bookingStringBuilder = StringBuilder()
            for (char in bookingText) {
                bookingStringBuilder.append(char)
                binding.tvBooking.text = bookingStringBuilder.toString()
                delay(100.milliseconds)
            }

            val destination = if (sharedPreferencesHelper.isLoggedIn) {
                MainActivity::class.java
            } else {
                LoginActivity::class.java
            }

            startActivity(Intent(this@SplashActivity, destination))
            finish()
        }
    }
}
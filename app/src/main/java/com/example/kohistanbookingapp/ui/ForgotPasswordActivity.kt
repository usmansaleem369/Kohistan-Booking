package com.example.kohistanbookingapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kohistanbookingapp.api.ApiRequestStatus
import com.example.kohistanbookingapp.databinding.ActivityForgotPasswordBinding
import com.example.kohistanbookingapp.models.request.ForgotPasswordRequest
import com.example.kohistanbookingapp.util.AppLoader
import com.example.kohistanbookingapp.util.AppUtils
import com.example.kohistanbookingapp.util.AppUtils.clearErrorOnTextChange
import com.example.kohistanbookingapp.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val authViewModel: AuthViewModel by viewModels()
    private var submittedIdentifier: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initUiAndListeners()
        observeViewModel()
    }

    private fun initUiAndListeners() {

        binding.etEmail.clearErrorOnTextChange()
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.tvBackToLogin.setOnClickListener {
            finish()
        }

        binding.btnSendOtp.setOnClickListener {
            val input = binding.etEmail.text.toString().trim()
            if (validateInput(input)) {
                submittedIdentifier = input
                // Trigger your forgot-password OTP endpoint
               authViewModel.forgotPassword(ForgotPasswordRequest(input))
            }
        }
    }

    private fun validateInput(input: String): Boolean {
        val trimmed = input.trim()

        if (trimmed.isEmpty()) {
            binding.etEmail.error = "Please enter your username or email address"
            binding.etEmail.requestFocus()
            return false
        }

        if (trimmed.contains("@")) {
            if (!Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
                binding.etEmail.error = "Please enter a valid email address"
                binding.etEmail.requestFocus()
                return false
            }
        } else if (trimmed.length < 3 || trimmed.contains(" ")) {
            binding.etEmail.error = "Username must be at least 3 characters with no spaces"
            binding.etEmail.requestFocus()
            return false
        }

        return true
    }

    private fun observeViewModel() {
        authViewModel.forgotPasswordResponse.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                AppUtils.showCustomToast(it.message, layoutInflater, this)

                val intent = Intent(this, ResetPasswordOtpActivity::class.java).apply {
                    putExtra(ResetPasswordOtpActivity.EXTRA_IDENTIFIER, submittedIdentifier)
                }
                startActivity(intent)
            }
        }

        authViewModel.apiRequestStatus.observe(this) { event ->
            event.getContentIfNotHandled()?.let { status ->
                when (status) {
                    is ApiRequestStatus.Loading -> AppLoader.instance.showLoader(this)
                    is ApiRequestStatus.Idle -> AppLoader.instance.hideLoader()
                    is ApiRequestStatus.Error -> {
                        AppLoader.instance.hideLoader()
                        AppUtils.showCustomToast(status.message, layoutInflater, this)
                    }
                }
            }
        }
    }
}
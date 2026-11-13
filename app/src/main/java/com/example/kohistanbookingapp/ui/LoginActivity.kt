package com.example.kohistanbookingapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kohistanbookingapp.api.ApiRequestStatus
import com.example.kohistanbookingapp.databinding.ActivityLoginBinding
import com.example.kohistanbookingapp.util.AppLoader
import com.example.kohistanbookingapp.util.AppUtils
import com.example.kohistanbookingapp.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val identifier = binding.etIdentifier.text?.toString()?.trim().orEmpty()
            val password = binding.etPassword.text?.toString()?.trim().orEmpty()

            if (validateInputs(identifier, password)) {
                authViewModel.login(identifier, password)
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot password clicked", Toast.LENGTH_SHORT).show()
        }

        binding.tvSignUp.setOnClickListener {
             startActivity(Intent(this, RegistrationActivity::class.java))
        }
    }

    private fun validateInputs(identifier: String, password: String): Boolean {
        var isValid = true

        if (identifier.isEmpty()) {
            binding.tilIdentifier.error = "Please enter email or phone"
            isValid = false
        } else {
            binding.tilIdentifier.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Please enter password"
            isValid = false
        } /*else if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        }*/ else {
            binding.tilPassword.error = null
        }

        return isValid
    }

    private fun observeViewModel() {
        authViewModel.apiRequestStatus.observe(this) { event ->
            event.getContentIfNotHandled()?.let { status ->
                when (status) {
                    is ApiRequestStatus.Loading -> {
                        AppLoader.instance.showLoader(this)
                    }
                    is ApiRequestStatus.Idle -> {
                        AppLoader.instance.hideLoader()
                    }
                    is ApiRequestStatus.Error -> {
                        AppLoader.instance.hideLoader()
                        AppUtils.showCustomToast(status.message,layoutInflater,this)
                    }
                }
            }
        }

        // If your ViewModel exposes a login success LiveData:
        authViewModel.loginResponse.observe(this) { event ->
            event.getContentIfNotHandled()?.let { response ->
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                navigateToHome()
            }
        }
    }


    private fun navigateToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
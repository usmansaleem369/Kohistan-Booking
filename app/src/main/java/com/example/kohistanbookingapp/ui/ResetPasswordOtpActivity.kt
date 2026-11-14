package com.example.kohistanbookingapp.ui

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kohistanbookingapp.R
import com.example.kohistanbookingapp.api.ApiRequestStatus
import com.example.kohistanbookingapp.databinding.ActivityResetPasswordOtpBinding
import com.example.kohistanbookingapp.models.request.ForgotPasswordRequest
import com.example.kohistanbookingapp.models.request.ResetPasswordRequest
import com.example.kohistanbookingapp.util.AppLoader
import com.example.kohistanbookingapp.util.AppUtils
import com.example.kohistanbookingapp.util.AppUtils.clearErrorOnTextChange
import com.example.kohistanbookingapp.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class ResetPasswordOtpActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_IDENTIFIER = "extra_identifier"
    }

    private lateinit var binding: ActivityResetPasswordOtpBinding
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var otpBoxes: List<EditText>
    private var countDownTimer: CountDownTimer? = null
    private var identifier: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityResetPasswordOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        identifier = intent.getStringExtra(EXTRA_IDENTIFIER).orEmpty()
        binding.tvOtpSentTo.text = "We sent a 6-digit verification code to $identifier"

        otpBoxes = listOf(
            binding.etOtp1,
            binding.etOtp2,
            binding.etOtp3,
            binding.etOtp4,
            binding.etOtp5,
            binding.etOtp6
        )

        setupOtpInputs()
        initUiAndListeners()
        observeViewModel()
        startResendTimer()
    }

    private fun setupOtpInputs() {
        binding.etNewPassword.clearErrorOnTextChange()
        binding.etConfirmPassword.clearErrorOnTextChange()

        otpBoxes.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val text = s?.toString().orEmpty()
                    if (text.length == 1 && index < otpBoxes.lastIndex) {
                        otpBoxes[index + 1].requestFocus()
                    }
                }
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL &&
                    event.action == KeyEvent.ACTION_DOWN &&
                    editText.text.isEmpty() &&
                    index > 0
                ) {
                    otpBoxes[index - 1].apply {
                        setText("")
                        requestFocus()
                    }
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun initUiAndListeners() {
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.tvResendOtp.setOnClickListener {
            otpBoxes.forEach { it.setText("") }
            otpBoxes.first().requestFocus()
            authViewModel.forgotPassword(ForgotPasswordRequest(identifier))
            startResendTimer()
        }

        binding.btnResetPassword.setOnClickListener {
            if (validateInputs()) {
                val otp = otpBoxes.joinToString("") { it.text.toString().trim() }
                val newPassword = binding.etNewPassword.text.toString()

                authViewModel.resetPassword(
                    ResetPasswordRequest(
                        userNameOrEmail = identifier,
                        otp = otp,
                        newPassword = newPassword,
                        confirmPassword = binding.etConfirmPassword.text.toString()
                    )
                )
            }
        }
    }

    private fun validateInputs(): Boolean {
        binding.etNewPassword.error = null
        binding.etConfirmPassword.error = null

        val otp = otpBoxes.joinToString("") { it.text.toString().trim() }
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (otp.length != 6) {
            AppUtils.showCustomToast("Please enter the complete 6-digit OTP", layoutInflater, this)
            otpBoxes.firstOrNull { it.text.isEmpty() }?.requestFocus()
            return false
        }

        if (newPassword.length < 6) {
            binding.etNewPassword.error = "Password must be at least 6 characters"
            binding.etNewPassword.requestFocus()
            return false
        }

        if (newPassword != confirmPassword) {
            binding.etConfirmPassword.error = "Passwords do not match"
            binding.etConfirmPassword.requestFocus()
            return false
        }

        return true
    }

    private fun startResendTimer() {
        countDownTimer?.cancel()
        binding.tvResendOtp.isEnabled = false
        binding.tvResendOtp.setTextColor(0xFF98A2B3.toInt())

        countDownTimer = object : CountDownTimer(60_000L, 1_000L) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.tvOtpTimer.text = String.format(
                    Locale.US,
                    "Resend code in 00:%02d",
                    seconds
                )
            }

            override fun onFinish() {
                binding.tvOtpTimer.text = "Didn't receive the code?"
                binding.tvResendOtp.isEnabled = true
                binding.tvResendOtp.setTextColor(
                    ContextCompat.getColor(this@ResetPasswordOtpActivity, R.color.btn_navy)
                )
            }
        }.start()
    }

    private fun observeViewModel() {
        authViewModel.resetPasswordResponse.observe(this) { event ->
          event.getContentIfNotHandled()?.let {
               AppUtils.showCustomToast("Password reset successfully! Please sign in.", layoutInflater, this)
               val intent = Intent(this, LoginActivity::class.java).apply {
                   flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
               }
               startActivity(intent)
               finishAffinity()
           }
       }

        authViewModel.forgotPasswordResponse.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                AppUtils.showCustomToast(it.message, layoutInflater, this)
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

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }
}
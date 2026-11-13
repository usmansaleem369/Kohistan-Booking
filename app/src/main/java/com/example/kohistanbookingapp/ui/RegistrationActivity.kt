package com.example.kohistanbookingapp.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kohistanbookingapp.R
import com.example.kohistanbookingapp.databinding.ActivityRegistrationBinding
import com.example.kohistanbookingapp.models.local.UserProfile
import com.example.kohistanbookingapp.util.AppUtils
import com.example.kohistanbookingapp.util.AppUtils.clearErrorOnTextChange
import com.example.kohistanbookingapp.util.SharedPreferencesHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RegistrationActivity : AppCompatActivity() {

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var binding: ActivityRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        prefillExistingProfileIfAvailable()
        setInputFieldsHelper()
        setupListeners()
    }

    private fun prefillExistingProfileIfAvailable() {
        val profile = sharedPreferencesHelper.userProfile ?: return
        binding.etFullName.setText(profile.fullName)
        binding.etCnic.setText(profile.cnic)
        binding.etPhone.setText(profile.phoneNo)
        binding.etEmail.setText(profile.email.orEmpty())
        binding.etCity.setText(profile.city.orEmpty())

        when (profile.title) {
            "Mrs" -> binding.rbMrs.isChecked = true
            "Miss" -> binding.rbMiss.isChecked = true
            else -> binding.rbMr.isChecked = true
        }

        if (profile.gender.equals("Female", ignoreCase = true)) {
            binding.rbFemale.isChecked = true
        } else {
            binding.rbMale.isChecked = true
        }
    }

    private fun setInputFieldsHelper() {
        binding.etFullName.clearErrorOnTextChange()
        binding.etCnic.clearErrorOnTextChange()
        binding.etPhone.clearErrorOnTextChange()
        binding.etEmail.clearErrorOnTextChange()
        binding.etPassword.clearErrorOnTextChange()

        binding.etCnic.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return
                isFormatting = true
                val digits = s.toString().replace("-", "")
                val formatted = buildString {
                    digits.forEachIndexed { index, c ->
                        if (index == 5 || index == 12) append('-')
                        append(c)
                    }
                }
                s.replace(0, s.length, formatted)
                isFormatting = false
            }
        })

        // Keep Title and Gender synced
        binding.rgTitle.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbMr -> binding.rbMale.isChecked = true
                R.id.rbMrs, R.id.rbMiss -> binding.rbFemale.isChecked = true
            }
        }
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnRegisterProfile.setOnClickListener {
            if (validateInputs()) {
                saveAndProceed()
            }
        }

        binding.tvAlreadyHaveAccount.setOnClickListener {
            // Navigate to LoginActivity if applicable, or close screen
            finish()
        }
    }

    private fun validateInputs(): Boolean {

        val name = binding.etFullName.text.toString().trim()
        val cnicDigits = binding.etCnic.text.toString().replace("-", "").trim()
        val phone = binding.etPhone.text.toString().replace("-", "").trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (name.length < 3) {
            binding.etFullName.error = "Please enter your full name (at least 3 characters)"
            binding.etFullName.requestFocus()
            return false
        }

        if (cnicDigits.length != 13) {
            binding.etCnic.error = "Please enter a valid 13-digit CNIC number"
            binding.etCnic.requestFocus()
            return false
        }

        if (!phone.matches(Regex("^03\\d{9}$"))) {
            binding.etPhone.error = "Enter a valid Pakistani mobile number (03XXXXXXXXX)"
            binding.etPhone.requestFocus()
            return false
        }

        if (email.isEmpty()) {
            binding.etEmail.error = "Email address is required"
            binding.etEmail.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Please enter a valid email address"
            binding.etEmail.requestFocus()
            return false
        }

        if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            binding.etPassword.requestFocus()
            return false
        }

        return true
    }

    private fun getSelectedTitle(): String = when (binding.rgTitle.checkedRadioButtonId) {
        R.id.rbMrs -> "Mrs"
        R.id.rbMiss -> "Miss"
        else -> "Mr"
    }

    private fun getSelectedGender(): String =
        if (binding.rbFemale.isChecked) "Female" else "Male"

    private fun saveAndProceed() {
        val profile = UserProfile(
            title = getSelectedTitle(),
            fullName = binding.etFullName.text.toString().trim(),
            cnic = binding.etCnic.text.toString().replace("-", "").trim(),
            gender = getSelectedGender(),
            phoneNo = binding.etPhone.text.toString().replace("-", "").trim(),
            email = binding.etEmail.text.toString().trim().ifEmpty { null },
            city = binding.etCity.text.toString().trim().ifEmpty { null }
        )

        // Save locally (and/or call your backend registration API via ApiViewModel here)
        sharedPreferencesHelper.userProfile = profile

        AppUtils.showCustomToast("Profile registered successfully!", layoutInflater, this)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finishAffinity()
    }
}
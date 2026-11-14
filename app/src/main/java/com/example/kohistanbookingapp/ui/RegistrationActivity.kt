package com.example.kohistanbookingapp.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kohistanbookingapp.api.ApiRequestStatus
import com.example.kohistanbookingapp.databinding.ActivityRegistrationBinding
import com.example.kohistanbookingapp.models.request.CreateProfileRequest
import com.example.kohistanbookingapp.util.AppLoader
import com.example.kohistanbookingapp.util.AppUtils
import com.example.kohistanbookingapp.util.AppUtils.clearErrorOnTextChange
import com.example.kohistanbookingapp.util.AppUtils.setupDobDatePicker
import com.example.kohistanbookingapp.util.SharedPreferencesHelper
import com.example.kohistanbookingapp.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RegistrationActivity : AppCompatActivity() {

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var binding: ActivityRegistrationBinding
    private val authViewModel: AuthViewModel by viewModels()

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
        initUiAndListeners()
        observeViewModel()
    }

    private fun initUiAndListeners() {
        listOf(
            binding.etFirstName, binding.etLastName, binding.etCnic, binding.etDob, binding.etNationality, binding.etUserName, binding.etPhone,
            binding.etEmail, binding.etAddress, binding.etPassword
        ).forEach { it.clearErrorOnTextChange() }

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

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        setupDobDatePicker(binding.etDob)

        binding.btnRegisterProfile.setOnClickListener {
            if (validateInputs()) {
                saveAndProceed()
            }
        }

        binding.tvAlreadyHaveAccount.setOnClickListener {
            finish()
        }
    }

    private fun prefillExistingProfileIfAvailable() {
        val profile = sharedPreferencesHelper.userProfile ?: return
        binding.etFirstName.setText(profile.firstName)
        binding.etLastName.setText(profile.lastName)
        binding.etCnic.setText(profile.cnic)
        binding.etDob.setText(profile.dob.substringBefore("T"))
        binding.etNationality.setText(profile.nationality.ifEmpty { "Pakistani" })
        binding.etUserName.setText(profile.userName)
        binding.etPhone.setText(profile.cellNo)
        binding.etEmail.setText(profile.email.orEmpty())
        binding.etAddress.setText(profile.address.orEmpty())

        if (profile.gender.equals("Female", ignoreCase = true)) {
            binding.rbFemale.isChecked = true
        } else {
            binding.rbMale.isChecked = true
        }
    }

    private fun validateInputs(): Boolean {
        clearFieldErrors()

        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val cnicDigits = binding.etCnic.text.toString().replace("-", "").trim()
        val dob = binding.etDob.text.toString().trim()
        val nationality = binding.etNationality.text.toString().trim()
        val userName = binding.etUserName.text.toString().trim()
        val phone = binding.etPhone.text.toString().replace("-", "").trim()
        val email = binding.etEmail.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (firstName.length < 2) {
            binding.etFirstName.error = "Please enter your first name"
            binding.etFirstName.requestFocus()
            return false
        }

        if (lastName.length < 2) {
            binding.etLastName.error = "Please enter your last name"
            binding.etLastName.requestFocus()
            return false
        }

        if (cnicDigits.length != 13) {
            binding.etCnic.error = "Please enter a valid 13-digit CNIC number"
            binding.etCnic.requestFocus()
            return false
        }

        if (dob.isEmpty()) {
            binding.etDob.error = "Please select your date of birth"
            binding.etDob.requestFocus()
            return false
        }

        if (nationality.isEmpty()) {
            binding.etNationality.error = "Nationality is required"
            binding.etNationality.requestFocus()
            return false
        }

        if (userName.length < 3) {
            binding.etUserName.error = "Username must be at least 3 characters"
            binding.etUserName.requestFocus()
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

        if (address.length < 5) {
            binding.etAddress.error = "Please enter your complete address"
            binding.etAddress.requestFocus()
            return false
        }

        if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            binding.etPassword.requestFocus()
            return false
        }

        return true
    }

    private fun clearFieldErrors() {
        binding.etFirstName.error = null
        binding.etLastName.error = null
        binding.etCnic.error = null
        binding.etDob.error = null
        binding.etNationality.error = null
        binding.etUserName.error = null
        binding.etPhone.error = null
        binding.etEmail.error = null
        binding.etAddress.error = null
        binding.etPassword.error = null
    }

    private fun getSelectedGender(): String =
        if (binding.rbFemale.isChecked) "Female" else "Male"

    private fun saveAndProceed() {
        val gender = getSelectedGender()
        val request = CreateProfileRequest(
            userName = binding.etUserName.text.toString().trim(),
            password = binding.etPassword.text.toString(),
            firstName = binding.etFirstName.text.toString().trim(),
            lastName = binding.etLastName.text.toString().trim(),
            gender = gender,
            dob = binding.etDob.text.toString().trim(),
            cnic = binding.etCnic.text.toString().replace("-", "").trim(),
            cellNo = binding.etPhone.text.toString().replace("-", "").trim(),
            email = binding.etEmail.text.toString().trim(),
            address = binding.etAddress.text.toString().trim(),
            nationality = binding.etNationality.text.toString().trim()
        )

        authViewModel.createProfile(request)
    }

    private fun observeViewModel() {
        authViewModel.createProfileResponse.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                AppUtils.showCustomToast("Profile created successfully!", layoutInflater, this)

                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finishAffinity()
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
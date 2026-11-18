package com.example.kohistanbookingapp.ui.activities

import android.content.Intent
import android.os.Bundle
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
import com.example.kohistanbookingapp.util.AppUtils.attachCnicFormatter
import com.example.kohistanbookingapp.util.AppUtils.clearErrorOnTextChange
import com.example.kohistanbookingapp.util.AppUtils.setupDobDatePicker
import com.example.kohistanbookingapp.util.AppUtils.toCleanDigits
import com.example.kohistanbookingapp.util.AppUtils.validateCnic
import com.example.kohistanbookingapp.util.AppUtils.validateEmail
import com.example.kohistanbookingapp.util.AppUtils.validatePakMobile
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

    private fun initUiAndListeners() = binding.apply {
        listOf(
            etFirstName, etLastName, etCnic, etDob, etNationality,
            etUserName, etPhone, etEmail, etAddress, etPassword
        ).forEach { it.clearErrorOnTextChange() }

        etCnic.attachCnicFormatter()
        setupDobDatePicker(etDob)

        ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnRegisterProfile.setOnClickListener {
            if (validateInputs()) {
                saveAndProceed()
            }
        }

        tvAlreadyHaveAccount.setOnClickListener {
            finish()
        }
    }

    private fun prefillExistingProfileIfAvailable() {
        val profile = sharedPreferencesHelper.userProfile ?: return
        binding.apply {
            etFirstName.setText(profile.firstName)
            etLastName.setText(profile.lastName)
            etCnic.setText(profile.cnic)
            etDob.setText(profile.dob.substringBefore("T"))
            etNationality.setText(profile.nationality.ifEmpty { "Pakistani" })
            etUserName.setText(profile.userName)
            etPhone.setText(profile.cellNo)
            etEmail.setText(profile.email.orEmpty())
            etAddress.setText(profile.address.orEmpty())

            if (profile.gender.equals("Female", ignoreCase = true)) {
                rbFemale.isChecked = true
            } else {
                rbMale.isChecked = true
            }
        }
    }

    private fun validateInputs(): Boolean {
        clearFieldErrors()

        binding.apply {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val dob = etDob.text.toString().trim()
            val nationality = etNationality.text.toString().trim()
            val userName = etUserName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val password = etPassword.text.toString()

            if (firstName.length < 2) {
                etFirstName.error = "Please enter your first name"
                etFirstName.requestFocus()
                return false
            }

            if (lastName.length < 2) {
                etLastName.error = "Please enter your last name"
                etLastName.requestFocus()
                return false
            }

            if (!etCnic.validateCnic()) return false

            if (dob.isEmpty()) {
                etDob.error = "Please select your date of birth"
                etDob.requestFocus()
                return false
            }

            if (nationality.isEmpty()) {
                etNationality.error = "Nationality is required"
                etNationality.requestFocus()
                return false
            }

            if (userName.length < 3) {
                etUserName.error = "Username must be at least 3 characters"
                etUserName.requestFocus()
                return false
            }

            if (!etPhone.validatePakMobile()) return false

            if (!etEmail.validateEmail()) return false

            if (address.length < 5) {
                etAddress.error = "Please enter your complete address"
                etAddress.requestFocus()
                return false
            }

            if (password.length < 6) {
                etPassword.error = "Password must be at least 6 characters"
                etPassword.requestFocus()
                return false
            }
        }

        return true
    }

    private fun clearFieldErrors() = binding.apply {
        listOf(
            etFirstName, etLastName, etCnic, etDob, etNationality,
            etUserName, etPhone, etEmail, etAddress, etPassword
        ).forEach { it.error = null }
    }

    private fun getSelectedGender(): String =
        if (binding.rbFemale.isChecked) "Female" else "Male"

    private fun saveAndProceed() = binding.apply {
        val request = CreateProfileRequest(
            userName = etUserName.text.toString().trim(),
            password = etPassword.text.toString(),
            firstName = etFirstName.text.toString().trim(),
            lastName = etLastName.text.toString().trim(),
            gender = getSelectedGender(),
            dob = etDob.text.toString().trim(),
            cnic = etCnic.text.toString().toCleanDigits(),
            cellNo = etPhone.text.toString().toCleanDigits(),
            email = etEmail.text.toString().trim(),
            address = etAddress.text.toString().trim(),
            nationality = etNationality.text.toString().trim()
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
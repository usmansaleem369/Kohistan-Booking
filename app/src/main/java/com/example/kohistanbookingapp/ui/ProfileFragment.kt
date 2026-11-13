package com.example.kohistanbookingapp.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.kohistanbookingapp.R
import com.example.kohistanbookingapp.databinding.FragmentProfileBinding
import com.example.kohistanbookingapp.models.local.UserProfile
import com.example.kohistanbookingapp.util.AppUtils
import com.example.kohistanbookingapp.util.SharedPreferencesHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInputHelpers()
        bindProfileData()
        setupListeners()
    }

    private fun bindProfileData() {
        val profile = sharedPreferencesHelper.userProfile ?: return
        // Read-only email field
        binding.etProfileEmail.setText(profile.email.orEmpty())

        // Editable fields
        binding.etProfileName.setText(profile.fullName)
        binding.etProfilePhone.setText(profile.phoneNo)
        binding.etProfileCnic.setText(profile.cnic)

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

    private fun setupInputHelpers() {
        // Auto-format CNIC as 33100-1234567-1
        binding.etProfileCnic.addTextChangedListener(object : TextWatcher {
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

        // Keep Title & Gender aligned when user switches Title
        binding.rgTitle.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbMr -> binding.rbMale.isChecked = true
                R.id.rbMrs, R.id.rbMiss -> binding.rbFemale.isChecked = true
            }
        }

        binding.etProfileName.clearErrorOnTextChange()
        binding.etProfilePhone.clearErrorOnTextChange()
        binding.etProfileCnic.clearErrorOnTextChange()
    }

    private fun setupListeners() {
        binding.btnUpdateProfile.setOnClickListener {
            if (validateInputs()) {
                saveUpdatedProfile()
            }
        }
    }

    private fun validateInputs(): Boolean {
        clearFieldErrors()

        val name = binding.etProfileName.text.toString().trim()
        val phone = binding.etProfilePhone.text.toString().replace("-", "").trim()
        val cnicDigits = binding.etProfileCnic.text.toString().replace("-", "").trim()

        if (name.length < 3) {
            binding.etProfileName.error = "Please enter your full name (at least 3 characters)"
            binding.etProfileName.requestFocus()
            return false
        }

        if (!phone.matches(Regex("^03\\d{9}$"))) {
            binding.etProfilePhone.error = "Enter a valid Pakistani mobile number (03XXXXXXXXX)"
            binding.etProfilePhone.requestFocus()
            return false
        }

        if (cnicDigits.length != 13) {
            binding.etProfileCnic.error = "Please enter a valid 13-digit CNIC number"
            binding.etProfileCnic.requestFocus()
            return false
        }

        return true
    }

    private fun clearFieldErrors() {
        binding.etProfileName.error = null
        binding.etProfilePhone.error = null
        binding.etProfileCnic.error = null
    }

    private fun saveUpdatedProfile() {
        val existingProfile = sharedPreferencesHelper.userProfile
        val updatedName = binding.etProfileName.text.toString().trim()
        val updatedPhone = binding.etProfilePhone.text.toString().replace("-", "").trim()
        val updatedCnic = binding.etProfileCnic.text.toString().replace("-", "").trim()
        val updatedTitle = when (binding.rgTitle.checkedRadioButtonId) {
            R.id.rbMrs -> "Mrs"
            R.id.rbMiss -> "Miss"
            else -> "Mr"
        }
        val updatedGender = if (binding.rbFemale.isChecked) "Female" else "Male"

        // Preserve existing immutable email & city
        val updatedProfile = UserProfile(
            title = updatedTitle,
            fullName = updatedName,
            cnic = updatedCnic,
            gender = updatedGender,
            phoneNo = updatedPhone,
            email = existingProfile?.email.orEmpty(),
            city = existingProfile?.city
        )

        sharedPreferencesHelper.userProfile = updatedProfile

        // Refresh top banner immediately

        AppUtils.showCustomToast(
            "Profile updated successfully!",
            layoutInflater,
            requireContext()
        )
    }

    private fun extractInitials(name: String): String {
        val parts = name.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        return when {
            parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "U"
        }
    }

    private fun EditText.clearErrorOnTextChange() {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (error != null) error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
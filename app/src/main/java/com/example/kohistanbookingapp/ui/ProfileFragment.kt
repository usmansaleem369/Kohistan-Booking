package com.example.kohistanbookingapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.kohistanbookingapp.api.ApiRequestStatus
import com.example.kohistanbookingapp.databinding.FragmentProfileBinding
import com.example.kohistanbookingapp.models.request.UpdateProfileRequest
import com.example.kohistanbookingapp.util.AppLoader
import com.example.kohistanbookingapp.util.AppUtils
import com.example.kohistanbookingapp.util.AppUtils.clearErrorOnTextChange
import com.example.kohistanbookingapp.util.SharedPreferencesHelper
import com.example.kohistanbookingapp.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()

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
        observeViewModel()
    }

    private fun setupInputHelpers() {
        binding.etCellNo.clearErrorOnTextChange()
        binding.etAddress.clearErrorOnTextChange()
    }

    private fun bindProfileData() {
        val profile = sharedPreferencesHelper.userProfile ?: return

        // Read-Only Identity & Email Fields
        binding.etFirstName.setText(profile.firstName)
        binding.etLastName.setText(profile.lastName)
        binding.etUserName.setText(profile.userName)
        binding.etProfileEmail.setText(profile.email)
        binding.etProfileCnic.setText(formatCnic(profile.cnic))
        binding.etProfileDob.setText(profile.dob.substringBefore("T"))
        binding.etNationality.setText(profile.nationality.ifEmpty { "Pakistani" })

        // Editable Fields
        binding.etCellNo.setText(profile.cellNo.replace("-", ""))
        binding.etAddress.setText(profile.address)

        if (profile.gender.equals("Female", ignoreCase = true)) {
            binding.rbFemale.isChecked = true
        } else {
            binding.rbMale.isChecked = true
        }
    }

    private fun formatCnic(raw: String): String {
        val digits = raw.replace("-", "")
        if (digits.length != 13) return raw
        return "${digits.substring(0, 5)}-${digits.substring(5, 12)}-${digits.substring(12)}"
    }

    private fun setupListeners() {
        binding.btnUpdateProfile.setOnClickListener {
            if (validateInputs()) {
                submitUpdateProfile()
            }
        }
    }

    private fun validateInputs(): Boolean {
        binding.etCellNo.error = null
        binding.etAddress.error = null

        val cellNo = binding.etCellNo.text.toString().replace("-", "").trim()
        val address = binding.etAddress.text.toString().trim()

        if (!cellNo.matches(Regex("^03\\d{9}$"))) {
            binding.etCellNo.error = "Enter a valid Pakistani mobile number (03XXXXXXXXX)"
            binding.etCellNo.requestFocus()
            return false
        }

        if (address.length < 5) {
            binding.etAddress.error = "Please enter your complete address"
            binding.etAddress.requestFocus()
            return false
        }

        return true
    }

    private fun getSelectedGender(): String =
        if (binding.rbFemale.isChecked) "Female" else "Male"

    private fun submitUpdateProfile() {
        val profile = sharedPreferencesHelper.userProfile ?: return
        val cleanCellNo = binding.etCellNo.text.toString().replace("-", "").trim()

        val request = UpdateProfileRequest(
            memberId = profile.memberId,
            cellNo = cleanCellNo,
            email = profile.email,
            address = binding.etAddress.text.toString().trim(),
            gender = getSelectedGender()
        )

        authViewModel.updateProfile(request)
    }

    private fun observeViewModel() {
        authViewModel.updateProfileResponse.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                AppUtils.showCustomToast(
                    "Profile updated successfully!",
                    layoutInflater,
                    requireContext()
                )
            }
        }

        authViewModel.apiRequestStatus.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { status ->
                when (status) {
                    is ApiRequestStatus.Loading -> AppLoader.instance.showLoader(requireContext())
                    is ApiRequestStatus.Idle -> AppLoader.instance.hideLoader()
                    is ApiRequestStatus.Error -> {
                        AppLoader.instance.hideLoader()
                        AppUtils.showCustomToast(status.message, layoutInflater, requireContext())
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
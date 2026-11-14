package com.example.kohistanbookingapp.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.kohistanbookingapp.R
import com.example.kohistanbookingapp.api.ApiRequestStatus
import com.example.kohistanbookingapp.databinding.ActivityMainBinding
import com.example.kohistanbookingapp.util.AppLoader
import com.example.kohistanbookingapp.util.AppUtils
import com.example.kohistanbookingapp.util.NavTab
import com.example.kohistanbookingapp.util.SharedPreferencesHelper
import com.example.kohistanbookingapp.viewmodel.ApiViewModel
import com.example.kohistanbookingapp.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    private lateinit var binding: ActivityMainBinding

    private val apiViewModel: ApiViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                checkLocationSettings()
            } else {
                // Location permission denied
                AppUtils.showCustomToast(
                    "Location permission is required.",
                    layoutInflater,
                    this,
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        checkLocationPermission()

        observeApiViewModel()

        initUiAndListeners()

        if (savedInstanceState == null) {
            selectTab(NavTab.HOME)
        }
    }

    private fun observeApiViewModel() {
        apiViewModel.apiRequestStatus.observe(this) { event ->
            event.getContentIfNotHandled()?.let { status ->
                handleApiStatus(status)
            }
        }
        authViewModel.getProfileResponse.observe(this){event ->
            event.getContentIfNotHandled()?.let {
                binding.tvDisplayName.text = getString(
                    R.string.full_name_data,
                    it.firstName,
                    it.lastName
                ).trim()

                binding.sideMenuContent.tvUserName.text = it.userName
            }
        }

        authViewModel.logoutResponse.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                sharedPreferencesHelper.clearUserData()
                apiViewModel.clearSession()
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finishAffinity()
            }
        }

    }

    private fun handleApiStatus(status: ApiRequestStatus?) {
        when (status) {
            is ApiRequestStatus.Loading -> {
                AppLoader.instance.showLoader(this)
            }

            is ApiRequestStatus.Idle -> {
                AppLoader.instance.hideLoader()
            }

            is ApiRequestStatus.Error -> {
                Log.e("Error API", status.message)
                AppLoader.instance.hideLoader()
                AppUtils.showCustomToast(status.message, layoutInflater, this)
            }

            else -> {
                AppLoader.instance.hideLoader()
            }
        }
    }

    private fun initUiAndListeners() = with(binding) {

        ivMenu.setOnClickListener {
            sideMenuContent.tvUserName.text = sharedPreferencesHelper.getUserSession()?.userName ?: ""
            sideMenuContent.tvUserEmail.text = sharedPreferencesHelper.email ?: ""
            if (motionLayout.progress == 0f) {
                motionLayout.transitionToEnd()
            } else {
                motionLayout.transitionToStart()
            }
        }


        mainContent.setOnClickListener {
            motionLayout.transitionToStart()
        }

        setupMenuClicks()

        if (sharedPreferencesHelper.userProfile == null) {
            authViewModel.getProfile(sharedPreferencesHelper.getUserSession()?.memberId ?: -1)
        }

    }

    private fun setupMenuClicks() {
        binding.bottomNavigation.apply {
            navHome.setOnClickListener { selectTab(NavTab.HOME) }
            navBookings.setOnClickListener { selectTab(NavTab.BOOKINGS) }
            navProfile.setOnClickListener { selectTab(NavTab.PROFILE) }
        }

        binding.sideMenuContent.apply {
            btnHome.setOnClickListener {
                selectTab(NavTab.HOME)
            }
            btnBookings.setOnClickListener {
                selectTab(NavTab.BOOKINGS)
            }
            btnProfile.setOnClickListener {
                selectTab(NavTab.PROFILE)
            }
            btnSettings.setOnClickListener {
                selectTab(NavTab.SETTINGS)
            }
            btnPrivacy.setOnClickListener {
                selectTab(NavTab.PRIVACY)
            }

            btnLogout.setOnClickListener {
                selectTab(NavTab.LOGOUT)
            }
        }
    }

    private fun selectTab(tab: NavTab) {
        val nav = binding.bottomNavigation
        binding.motionLayout.transitionToStart()
        // Update selected state (triggers tint & text color changes)
        nav.navHome.isSelected = (tab == NavTab.HOME)
        nav.ivHome.isSelected = (tab == NavTab.HOME)
        nav.tvHome.isSelected = (tab == NavTab.HOME)

        nav.navBookings.isSelected = (tab == NavTab.BOOKINGS)
        nav.ivBookings.isSelected = (tab == NavTab.BOOKINGS)
        nav.tvBookings.isSelected = (tab == NavTab.BOOKINGS)

        nav.navProfile.isSelected = (tab == NavTab.PROFILE)
        nav.ivProfile.isSelected = (tab == NavTab.PROFILE)
        nav.tvProfile.isSelected = (tab == NavTab.PROFILE)

        binding.sideMenuContent.apply {
            btnHome.isSelected = (tab == NavTab.HOME)
            btnProfile.isSelected = (tab == NavTab.PROFILE)
            btnBookings.isSelected = (tab == NavTab.BOOKINGS)
            btnSettings.isSelected = (tab == NavTab.SETTINGS)
            btnPrivacy.isSelected = (tab == NavTab.PRIVACY)
            btnLogout.isSelected = (tab == NavTab.LOGOUT)
        }

        // Switch Fragment
        when (tab) {
            NavTab.HOME -> loadFragment(HomeFragment())
            NavTab.BOOKINGS -> loadFragment(BookingsFragment())
            NavTab.PROFILE -> loadFragment(ProfileFragment())
            NavTab.SETTINGS -> {}
            NavTab.PRIVACY -> {
                startActivity(Intent(this, PrivacyActivity::class.java))
            }
            NavTab.LOGOUT -> {
                authViewModel.logout()
            }
        }
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.frameLayout.id, fragment)
            .commit()
    }

    fun loadFragmentBackStack(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.frameLayout.id, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun checkLocationPermission() {
        if (!AppUtils.isLocationPermissionGranted(this)) {

            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )

        } else {
            checkLocationSettings()
        }
    }

    private fun checkLocationSettings() {
        if (!AppUtils.isLocationEnabled(this)) {
            AppUtils.openLocationSettings(this)
        }
    }

    override fun onResume() {
        super.onResume()

        sharedPreferencesHelper.userProfile?.let {
            binding.tvDisplayName.text = getString(
                R.string.full_name_data,
                it.firstName,
                it.lastName
            ).trim()
            binding.sideMenuContent.tvUserName.text = it.userName
            binding.sideMenuContent.tvUserEmail.text = it.email
        }
        if (::binding.isInitialized &&
            AppUtils.isLocationPermissionGranted(this)
        ) {
            checkLocationSettings()
        }
    }


}

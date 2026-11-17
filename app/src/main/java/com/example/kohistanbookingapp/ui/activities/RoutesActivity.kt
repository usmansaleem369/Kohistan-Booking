package com.example.kohistanbookingapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kohistanbookingapp.R
import com.example.kohistanbookingapp.adapters.RouteTimeAdapter
import com.example.kohistanbookingapp.api.ApiRequestStatus
import com.example.kohistanbookingapp.databinding.ActivityRoutesBinding
import com.example.kohistanbookingapp.util.AppLoader
import com.example.kohistanbookingapp.util.AppUtils
import com.example.kohistanbookingapp.util.AppUtils.setupTravelDatePicker
import com.example.kohistanbookingapp.util.SharedPreferencesHelper
import com.example.kohistanbookingapp.viewmodel.CreateBookingViewModel
import com.example.kohistanbookingapp.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RoutesActivity : AppCompatActivity() {
    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    private lateinit var binding: ActivityRoutesBinding
    private val createBookingViewModel: CreateBookingViewModel by viewModels()

   private val sharedViewModel: SharedViewModel by viewModels()
    private lateinit var routeTimeAdapter: RouteTimeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoutesBinding.inflate(layoutInflater)
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
        setupDatePicker()
        createBookingViewModel.getRouteDetails(sharedPreferencesHelper.selectedTravelDate?:"")
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        routeTimeAdapter = RouteTimeAdapter { selectedRouteTime ->
            sharedPreferencesHelper.selectedRouteData = selectedRouteTime
            val intent = Intent(this, SelectSeatActivity::class.java)
            startActivity(intent)
        }
        binding.rvRouteTimes.adapter = routeTimeAdapter

    }

    private fun observeViewModel() {

        binding.tvSelectedCities.text = getString(
            R.string.to,
            sharedViewModel.selectedFromCity.value?.cityName,
            sharedViewModel.selectedToCity.value?.cityName
        )

        createBookingViewModel.routeDetailResponse.observe(this) { event ->
         event.getContentIfNotHandled()?.let {routes->
             val allTimes = routes.distinctBy { it.routeId }
             if (allTimes.isEmpty()) {
                 binding.tvEmptyState.visibility = View.VISIBLE
                 binding.rvRouteTimes.visibility = View.GONE
             } else {
                 binding.tvEmptyState.visibility = View.GONE
                 binding.rvRouteTimes.visibility = View.VISIBLE
                 routeTimeAdapter.submitRoutes(allTimes,sharedPreferencesHelper.selectedTravelDate?:"")
             }
         }
        }
        createBookingViewModel.apiRequestStatus.observe(this) { event ->
            event.getContentIfNotHandled()?.let { status ->
                handleApiStatus(status)
            }

        }
    }

    private fun handleApiStatus(status: ApiRequestStatus) {

        when (status) {
            is ApiRequestStatus.Loading -> {
                AppLoader.instance.showLoader(this)
            }

            is ApiRequestStatus.Idle -> {
                AppLoader.instance.hideLoader()
            }

            is ApiRequestStatus.Error -> {
                AppLoader.instance.hideLoader()
                AppUtils.showCustomToast(status.message, layoutInflater, this)
            }
        }

    }


    private fun setupDatePicker() {
        setupTravelDatePicker(
            binding.btnCalendar,
            sharedPreferencesHelper = sharedPreferencesHelper,
            title = "Select Travel Date"
        ) { formattedDate ->
            createBookingViewModel.getRouteDetails(formattedDate)
        }
    }

}
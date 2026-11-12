package com.example.kohistanbookingapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.kohistanbookingapp.R
import com.example.kohistanbookingapp.adapters.RouteTimeAdapter
import com.example.kohistanbookingapp.api.ApiRequestStatus
import com.example.kohistanbookingapp.databinding.ActivityRoutesBinding
import com.example.kohistanbookingapp.util.AppLoader
import com.example.kohistanbookingapp.util.AppUtils
import com.example.kohistanbookingapp.util.SharedPreferencesHelper
import com.example.kohistanbookingapp.viewmodel.ApiViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@AndroidEntryPoint
class RoutesActivity : AppCompatActivity() {
    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    private lateinit var binding: ActivityRoutesBinding
    private val apiViewModel: ApiViewModel by viewModels()
    private lateinit var routeTimeAdapter: RouteTimeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoutesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()
        initUiAndListeners()
    }

    private fun initUiAndListeners() {
        setupDatePicker()
        apiViewModel.getRouteDetails(sharedPreferencesHelper.selectedTravelDate?:"")
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun setupRecyclerView() {
        routeTimeAdapter = RouteTimeAdapter(
           /* fromCity = apiViewModel.selectedFromCity.value?.cityName ?: "",
            toCity = apiViewModel.selectedToCity.value?.cityName ?: ""*/
        ) { selectedRouteTime ->
            sharedPreferencesHelper.selectedRouteData = selectedRouteTime
           val intent = Intent(this, SelectSeatActivity::class.java)
            startActivity(intent)
        }
        binding.rvRouteTimes.adapter = routeTimeAdapter
    }

    private fun observeViewModel() {

        binding.tvSelectedCities.text = getString(
            R.string.to,
            apiViewModel.selectedFromCity.value?.cityName,
            apiViewModel.selectedToCity.value?.cityName
        )

        apiViewModel.routeDetailResponse.observe(this) { event ->
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
        apiViewModel.apiRequestStatus.observe(this) { event ->
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
        binding.btnCalendar.setOnClickListener {

            val utcTimeZone = TimeZone.getTimeZone("UTC")
            val dateFormat = SimpleDateFormat(
                "EEE, dd MMM yyyy",
                Locale.ENGLISH
            ).apply {
                timeZone = utcTimeZone
            }

            val selectedDate = sharedPreferencesHelper.selectedTravelDate

            val selectedTravelDateMillis = runCatching {
                dateFormat.parse(selectedDate?:"")?.time
            }.getOrNull() ?: MaterialDatePicker.todayInUtcMilliseconds()

            val constraints = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .build()

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Travel Date")
                .setTheme(R.style.ThemeOverlay_App_DatePicker_Gold)
                .setSelection(selectedTravelDateMillis)
                .setCalendarConstraints(constraints)
                .build()

            datePicker.addOnPositiveButtonClickListener { selectionMillis ->
                val formattedDate = dateFormat.format(Date(selectionMillis))
                apiViewModel.getRouteDetails(formattedDate)
                sharedPreferencesHelper.selectedTravelDate = formattedDate
            }

            datePicker.show(supportFragmentManager, "TRAVEL_DATE_PICKER")
        }
    }

}
package com.example.kohistanbookingapp.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.kohistanbookingapp.R
import com.example.kohistanbookingapp.adapters.RouteTimeAdapter
import com.example.kohistanbookingapp.databinding.ActivityRoutesBinding
import com.example.kohistanbookingapp.viewmodel.ApiViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@AndroidEntryPoint
class RoutesActivity : AppCompatActivity() {

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

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        Log.d("SearchBuses", " ${apiViewModel.getSelectedDate()}")
        apiViewModel.getRouteDetails(apiViewModel.getSelectedDate())
    }

    private fun setupRecyclerView() {
        routeTimeAdapter = RouteTimeAdapter(
           /* fromCity = apiViewModel.selectedFromCity.value?.cityName ?: "",
            toCity = apiViewModel.selectedToCity.value?.cityName ?: ""*/
        ) { selectedRouteTime ->
            Toast.makeText(
                this,
                "Selected departure: ${selectedRouteTime}",
                Toast.LENGTH_SHORT
            ).show()
            // Navigate to seat selection screen passing selectedRouteTime.routeTimeId
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
             // Flattens timing collections from all matching routes
             val allTimes = routes.flatMap {routes }
             if (allTimes.isEmpty()) {
                 binding.tvEmptyState.visibility = View.VISIBLE
                 binding.rvRouteTimes.visibility = View.GONE
             } else {
                 binding.tvEmptyState.visibility = View.GONE
                 binding.rvRouteTimes.visibility = View.VISIBLE
                 routeTimeAdapter.submitList(allTimes)
             }
         }
        }
        /*apiViewModel.selectedUtcDate.observe(this) { city ->
            apiViewModel.getRouteDetails()
        }*/

    }

    fun setupDatePicker() {
        val openPickerListener = View.OnClickListener {
            val constraints = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .build()

            val selectedTravelDateMillis: Long = MaterialDatePicker.todayInUtcMilliseconds()

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Travel Date")
                .setTheme(R.style.ThemeOverlay_App_DatePicker_Gold)
                .setSelection(selectedTravelDateMillis)
                .setCalendarConstraints(constraints)
                .build()

            datePicker.addOnPositiveButtonClickListener { selectionMillis ->
                val utcFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val formattedDate = utcFormat.format(Date(selectionMillis))
                apiViewModel.getRouteDetails(formattedDate)
            }

            datePicker.show(supportFragmentManager, "TRAVEL_DATE_PICKER")
        }

        binding.btnCalendar.setOnClickListener(openPickerListener)
    }
}
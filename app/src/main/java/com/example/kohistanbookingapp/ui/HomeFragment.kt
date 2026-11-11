package com.example.kohistanbookingapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.kohistanbookingapp.R
import com.example.kohistanbookingapp.adapters.SearchableDropdownPopup
import com.example.kohistanbookingapp.databinding.FragmentHomeBinding
import com.example.kohistanbookingapp.models.response.CityData
import com.example.kohistanbookingapp.util.AppUtils
import com.example.kohistanbookingapp.viewmodel.ApiViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val apiViewModel: ApiViewModel by activityViewModels()

    private var selectedTravelDateMillis: Long = MaterialDatePicker.todayInUtcMilliseconds()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        initUiAndListeners()
    }

    private fun initUiAndListeners() {
        apiViewModel.citiesData.value?.lastContent()?.let {cities->
            setupSearchableDropdowns(cities)
        }?:run {
            apiViewModel.getAllCities()
        }

        if ( apiViewModel.allRoutesData.value?.lastContent() == null){
            apiViewModel.getAllRoutes()
        }

        setupDatePicker()

        binding.btnSearchBuses.setOnClickListener {
           validateAndProceed()
        }

        binding.btnSwapLocations.setOnClickListener {
            apiViewModel.swapCities()
        }
    }

    private fun validateAndProceed() {
        val fromCity = apiViewModel.selectedFromCity.value
        val toCity = apiViewModel.selectedToCity.value

        var isValid = true

        if (fromCity == null) {
            binding.tilFrom.error = "Select departure city"
            isValid = false
        } else {
            binding.tilFrom.error = null
            binding.tilFrom.isErrorEnabled = false
        }

        if (toCity == null) {
            binding.tilTo.error = "Select destination city"
            isValid = false
        } else {
            binding.tilTo.error = null
            binding.tilTo.isErrorEnabled = false
        }

        if (!isValid) return

        Log.d("SearchBuses", "From: ${fromCity?.cityName} -> To: ${toCity?.cityName}")
        apiViewModel.searchRoutes()

    }

    private fun observeViewModel() {
        apiViewModel.citiesData.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { cities ->
                setupSearchableDropdowns(cities)
            }
        }

        apiViewModel.selectedFromCity.observe(viewLifecycleOwner) { city ->
            binding.actFrom.setText(city?.cityName.orEmpty(), false)
            if (city != null) {
                binding.tilFrom.error = null
                binding.tilFrom.isErrorEnabled = false
            }
        }

        apiViewModel.selectedToCity.observe(viewLifecycleOwner) { city ->
            binding.actTo.setText(city?.cityName.orEmpty(), false)
            if (city != null) {
                binding.tilTo.error = null
                binding.tilTo.isErrorEnabled = false
            }
        }

    }

    private fun setupSearchableDropdowns(cities: List<CityData>) {

        binding.actFrom.setOnClickListener {
            val availableFromCities = cities.filter { it.cityId != apiViewModel.selectedToCity.value?.cityId }
            SearchableDropdownPopup(context = requireContext(), anchorView = binding.tilFrom, cities = availableFromCities) { selectedCity ->
                apiViewModel.setFromCity(selectedCity)
            }.show()
        }

        binding.actTo.setOnClickListener {
            val availableFromCities = cities.filter { it.cityId != apiViewModel.selectedFromCity.value?.cityId }
            SearchableDropdownPopup(context = requireContext(), anchorView = binding.tilTo, cities = availableFromCities) { selectedCity ->
                apiViewModel.setToCity(selectedCity)
            }.show()
        }
    }

    private fun setupDatePicker() {
        val displayFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        binding.etTravelDate.setText(displayFormat.format(Date()))

        val openPickerListener = View.OnClickListener {
            val constraints = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .build()

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Travel Date")
                .setTheme(R.style.ThemeOverlay_App_DatePicker_Gold) // <-- Applied here
                .setSelection(selectedTravelDateMillis)
                .setCalendarConstraints(constraints)
                .build()

            datePicker.addOnPositiveButtonClickListener { selectionMillis ->
                selectedTravelDateMillis = selectionMillis
                val utcFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val formattedDate = utcFormat.format(Date(selectionMillis))
                binding.etTravelDate.setText(formattedDate)
                apiViewModel.setSelectedDate(formattedDate)
            }

            datePicker.show(parentFragmentManager, "TRAVEL_DATE_PICKER")
        }

        binding.etTravelDate.setOnClickListener(openPickerListener)
        binding.tilDate.setStartIconOnClickListener(openPickerListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
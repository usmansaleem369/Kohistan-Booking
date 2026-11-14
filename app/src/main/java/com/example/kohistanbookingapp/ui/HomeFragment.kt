package com.example.kohistanbookingapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.kohistanbookingapp.R
import com.example.kohistanbookingapp.adapters.SearchableDropdownPopup
import com.example.kohistanbookingapp.databinding.FragmentHomeBinding
import com.example.kohistanbookingapp.models.response.CityData
import com.example.kohistanbookingapp.util.AppUtils
import com.example.kohistanbookingapp.util.AppUtils.setupTravelDatePicker
import com.example.kohistanbookingapp.util.SharedPreferencesHelper
import com.example.kohistanbookingapp.viewmodel.ApiViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {
    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
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
        binding.tvTime.text = AppUtils.getTimeGreeting()

        apiViewModel.allDepartureCities.value?.lastContent()?.let { cities->
            setupFromDropdowns(cities)
        }?:run {
            apiViewModel.getAllDepartureCities()
        }

        apiViewModel.allDestinationCities.value?.lastContent()?.let { cities->
            setupToDropdowns(cities)
        }?:run {
            apiViewModel.getAllDestinationCities()
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
        sharedPreferencesHelper.selectedTravelDate = binding.etTravelDate.text.toString()
        Log.d("SearchBuses", "From: ${fromCity?.cityName} -> To: ${toCity?.cityName} -> ${sharedPreferencesHelper.selectedTravelDate}")
        val intent = Intent(requireActivity(), RoutesActivity::class.java)
        startActivity(intent)

    }

    private fun observeViewModel() {
        apiViewModel.allDepartureCities.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { cities ->
                setupFromDropdowns(cities)
            }
        }

        apiViewModel.allDestinationCities.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { cities ->
                setupToDropdowns(cities)
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

    private fun setupFromDropdowns(cities: List<CityData>) {
        binding.actFrom.setOnClickListener {
            val availableFromCities = cities.filter { it.cityId != apiViewModel.selectedToCity.value?.cityId }
            SearchableDropdownPopup(context = requireContext(), anchorView = binding.tilFrom, cities = availableFromCities) { selectedCity ->
                apiViewModel.setFromCity(selectedCity)
                apiViewModel.getAllDestinationCities()
            }.show()
        }
    }

    private fun setupToDropdowns(cities: List<CityData>) {
         binding.actTo.setOnClickListener {
             val availableFromCities = cities.filter { it.cityId != apiViewModel.selectedFromCity.value?.cityId }
             SearchableDropdownPopup(context = requireContext(), anchorView = binding.tilTo, cities = availableFromCities) { selectedCity ->
                 apiViewModel.setToCity(selectedCity)
             }.show()
         }
    }

    private fun setupDatePicker() {
        setupTravelDatePicker(
            editText = binding.etTravelDate,
            textInputLayout = binding.tilDate,
            title = "Select Travel Date",
            sharedPreferencesHelper = sharedPreferencesHelper
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.example.kohistanbookingapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.kohistanbookingapp.adapters.SearchableDropdownPopup
import com.example.kohistanbookingapp.databinding.FragmentHomeBinding
import com.example.kohistanbookingapp.models.response.CityData
import com.example.kohistanbookingapp.ui.activities.RoutesActivity
import com.example.kohistanbookingapp.util.AppUtils
import com.example.kohistanbookingapp.util.AppUtils.setupTravelDatePicker
import com.example.kohistanbookingapp.util.SharedPreferencesHelper
import com.example.kohistanbookingapp.viewmodel.CreateBookingViewModel
import com.example.kohistanbookingapp.viewmodel.SharedViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {
    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val createBookingViewModel: CreateBookingViewModel by activityViewModels()

    private val sharedViewModel: SharedViewModel by activityViewModels()

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

        createBookingViewModel.allDepartureCities.value?.lastContent()?.let { cities->
            setupFromDropdowns(cities)
        }?:run {
            createBookingViewModel.getAllDepartureCities()
        }

        createBookingViewModel.allDestinationCities.value?.lastContent()?.let { cities->
            setupToDropdowns(cities)
        }?:run {
            createBookingViewModel.getAllDestinationCities()
        }

        setupDatePicker()

        binding.btnSearchBuses.setOnClickListener {
           validateAndProceed()
        }

        binding.btnSwapLocations.setOnClickListener {
            sharedViewModel.swapCities()
        }
    }

    private fun validateAndProceed() {
        val fromCity = sharedViewModel.selectedFromCity.value
        val toCity = sharedViewModel.selectedToCity.value

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
        createBookingViewModel.allDepartureCities.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { cities ->
                setupFromDropdowns(cities)
            }
        }

        createBookingViewModel.allDestinationCities.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { cities ->
                setupToDropdowns(cities)
            }
        }

        sharedViewModel.selectedFromCity.observe(viewLifecycleOwner) { city ->
            binding.actFrom.setText(city?.cityName.orEmpty(), false)
            if (city != null) {
                binding.tilFrom.error = null
                binding.tilFrom.isErrorEnabled = false
            }
        }

        sharedViewModel.selectedToCity.observe(viewLifecycleOwner) { city ->
            binding.actTo.setText(city?.cityName.orEmpty(), false)
            if (city != null) {
                binding.tilTo.error = null
                binding.tilTo.isErrorEnabled = false
            }
        }

    }

    private fun setupFromDropdowns(cities: List<CityData>) {
        binding.actFrom.setOnClickListener {
            val availableFromCities = cities.filter { it.cityId != sharedViewModel.selectedToCity.value?.cityId }
            SearchableDropdownPopup(context = requireContext(), anchorView = binding.tilFrom, cities = availableFromCities) { selectedCity ->
                sharedViewModel.setFromCity(selectedCity)
                createBookingViewModel.getAllDestinationCities()
            }.show()
        }
    }

    private fun setupToDropdowns(cities: List<CityData>) {
         binding.actTo.setOnClickListener {
             val availableFromCities = cities.filter { it.cityId != sharedViewModel.selectedFromCity.value?.cityId }
             SearchableDropdownPopup(context = requireContext(), anchorView = binding.tilTo, cities = availableFromCities) { selectedCity ->
                 sharedViewModel.setToCity(selectedCity)
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
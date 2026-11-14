package com.example.kohistanbookingapp.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kohistanbookingapp.R
import com.example.kohistanbookingapp.api.ApiRequestStatus
import com.example.kohistanbookingapp.databinding.ActivityPassengerDetailsBinding
import com.example.kohistanbookingapp.models.request.BookingSeatItem
import com.example.kohistanbookingapp.models.request.CreateBookingRequest
import com.example.kohistanbookingapp.models.response.SeatGender
import com.example.kohistanbookingapp.models.response.SelectedSeat
import com.example.kohistanbookingapp.util.AppLoader
import com.example.kohistanbookingapp.util.AppUtils
import com.example.kohistanbookingapp.util.AppUtils.clearErrorOnTextChange
import com.example.kohistanbookingapp.util.SharedPreferencesHelper
import com.example.kohistanbookingapp.viewmodel.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class PassengerDetailsActivity : AppCompatActivity() {

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var binding: ActivityPassengerDetailsBinding
    private val apiViewModel: ApiViewModel by viewModels()

    private var selectedSeats: List<SelectedSeat> = emptyList()
    private var totalAmount: Double = 0.0
    private val currencyFormat = NumberFormat.getIntegerInstance(Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPassengerDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        selectedSeats = sharedPreferencesHelper.selectedSeats

        initUiAndListeners()
        observeViewModel()
    }

    private fun initUiAndListeners() {
        showBookedTicketDetails()
        setInputFieldsHelper()
        prefillSavedUserProfile()

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnPayWithJazzCash.setOnClickListener {
            if (validateInputs()) {
                submitBookingRequest()
            }
        }
    }

    private fun prefillSavedUserProfile() {
        val profile = sharedPreferencesHelper.userProfile
        if (profile == null) {
            autoSelectDefaultTitle()
            return
        }

        binding.llPassengerDetails.apply {
            if (profile.gender.equals("Female", ignoreCase = true)) {
                rbMiss.isChecked = true
            } else {
                rbMr.isChecked = true
            }
            etPassengerName.setText(
                getString(
                    R.string.full_name_data,
                    profile.firstName,
                    profile.lastName
                ).trim())
            etPassengerCnic.setText(profile.cnic)
            etPassengerPhone.setText(profile.cellNo)
            etPassengerEmail.setText(profile.email.orEmpty())
        }

        binding.llPaymentMethod.etJazzCashAccount.setText(profile.cellNo)
    }

    private fun showBookedTicketDetails() {
        val route = sharedPreferencesHelper.selectedRouteData
        val travelDate = sharedPreferencesHelper.selectedTravelDate.orEmpty()
        val formattedTime = AppUtils.formatToAmPm(route?.routeTimeValue)

        binding.tvSubHeader.text = listOf(
            "${route?.fromCity.orEmpty()} to ${route?.toCity.orEmpty()}",
            formattedTime
        ).filter { it.isNotBlank() }.joinToString(" • ")

        binding.llAllTicketsDetail.tvSummaryRoute.text = getString(R.string.to_from, route?.fromCity, route?.toCity)
        binding.llAllTicketsDetail.tvVehicleBadge.text = route?.vehicleType.orEmpty()
        binding.llAllTicketsDetail.tvSummaryDateTime.text = listOf(travelDate, formattedTime)
            .filter { it.isNotBlank() }
            .joinToString(" • ")

        binding.llAllTicketsDetail.tvSummarySeats.text = selectedSeats
            .sortedBy { it.seatNumber }
            .joinToString(", ") {
                val tag = if (it.assignedGender == SeatGender.FEMALE) "F" else "M"
                "${it.seatNumber}($tag)"
            }

        totalAmount = selectedSeats.size * (route?.fare ?: 0.0)
        binding.tvTotalPayable.text = getString(R.string.pkr, currencyFormat.format(totalAmount))
    }

    private fun autoSelectDefaultTitle() {
        val firstSeatGender = selectedSeats.firstOrNull()?.assignedGender
        if (firstSeatGender == SeatGender.FEMALE) {
            binding.llPassengerDetails.rbMiss.isChecked = true
        } else {
            binding.llPassengerDetails.rbMr.isChecked = true
        }
    }

    private fun getSelectedTitle(): String {
        return when (binding.llPassengerDetails.rgTitle.checkedRadioButtonId) {
            R.id.rbMrs -> "Mrs"
            R.id.rbMiss -> "Miss"
            else -> "Mr"
        }
    }

    private fun setInputFieldsHelper() {
        binding.llPassengerDetails.etPassengerCnic.addTextChangedListener(object : TextWatcher {
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

        binding.llPassengerDetails.etPassengerPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!binding.llPaymentMethod.etJazzCashAccount.hasFocus()) {
                    binding.llPaymentMethod.etJazzCashAccount.setText(s?.toString().orEmpty())
                }
            }
        })

        binding.llPassengerDetails.apply {
            etPassengerName.clearErrorOnTextChange()
            etPassengerCnic.clearErrorOnTextChange()
            etPassengerPhone.clearErrorOnTextChange()
            etPassengerEmail.clearErrorOnTextChange()
        }
        binding.llPaymentMethod.etJazzCashAccount.clearErrorOnTextChange()
    }

    private fun validateInputs(): Boolean {

        if (selectedSeats.isEmpty()) {
            AppUtils.showCustomToast("No seats selected. Please go back and select seats.", layoutInflater, this)
            return false
        }

        binding.llPassengerDetails.apply {
            val name = etPassengerName.text.toString().trim()
            val cnicDigits = etPassengerCnic.text.toString().replace("-", "").trim()
            val phone = etPassengerPhone.text.toString().replace("-", "").trim()
            val email = etPassengerEmail.text.toString().trim()
            val jazzCashNumber = binding.llPaymentMethod.etJazzCashAccount.text.toString().replace("-", "").trim()

            if (name.length < 3) {
                etPassengerName.error = "Please enter valid passenger full name (at least 3 characters)"
                etPassengerName.requestFocus()
                return false
            }

            if (cnicDigits.length != 13) {
                etPassengerCnic.error = "Please enter a valid 13-digit CNIC number"
                etPassengerCnic.requestFocus()
                return false
            }

            if (!phone.matches(Regex("^03\\d{9}$"))) {
                etPassengerPhone.error = "Enter a valid Pakistani mobile number (03XXXXXXXXX)"
                etPassengerPhone.requestFocus()
                return false
            }

            if (email.isEmpty()) {
                etPassengerEmail.error = "Email address is required"
                etPassengerEmail.requestFocus()
                return false
            }

            if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etPassengerEmail.error = "Please enter a valid email address"
                etPassengerEmail.requestFocus()
                return false
            }

            if (!jazzCashNumber.matches(Regex("^03\\d{9}$"))) {
                binding.llPaymentMethod.etJazzCashAccount.error = "Enter a valid 11-digit JazzCash account number (03XXXXXXXXX)"
                binding.llPaymentMethod.etJazzCashAccount.requestFocus()
                return false
            }

            return true
        }
    }


    private fun submitBookingRequest() {
        val route = sharedPreferencesHelper.selectedRouteData ?: run {
            AppUtils.showCustomToast("Route information missing. Please search again.", layoutInflater, this)
            return
        }
        val seatItems = selectedSeats
            .sortedBy { it.seatNumber }
            .map { seat ->
                BookingSeatItem(
                    seatNo = seat.seatNumber,
                    gender = if (seat.assignedGender == SeatGender.FEMALE) "Female" else "Male"
                )
            }
        val request = CreateBookingRequest(
            terminalId = route.fromCityId ?: 0,
            routeId = route.routeId ?: 0,
            dropPointId = route.dropPointId ?: 0,
            vehicleTypeId = route.vehicleTypeId ?: 0,
            routeTimeId = route.routeTimeId ?: 0L,
            departureDate = sharedPreferencesHelper.selectedTravelDate.orEmpty(),
            seats = seatItems,
            title = getSelectedTitle(),
            passengerName = binding.llPassengerDetails.etPassengerName.text.toString().trim(),
            cnic = binding.llPassengerDetails.etPassengerCnic.text.toString().replace("-", "").trim(),
            contactNo = binding.llPassengerDetails.etPassengerPhone.text.toString().replace("-", "").trim(),
            email = binding.llPassengerDetails.etPassengerEmail.text.toString().trim().ifEmpty { null }
        )

        apiViewModel.createBooking(request)
    }

    private fun observeViewModel() {
        apiViewModel.createBookingResponse.observe(this) { event ->
            event.getContentIfNotHandled()?.let { bookingData ->
                AppUtils.showCustomToast(
                    "Seats reserved! Reservation ID: ${bookingData.reservationId}",
                    layoutInflater,
                    this
                )

                sharedPreferencesHelper.clearTicketSession()
                apiViewModel.clearSession()
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finishAffinity()
            }
        }

        apiViewModel.apiRequestStatus.observe(this) { event ->
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
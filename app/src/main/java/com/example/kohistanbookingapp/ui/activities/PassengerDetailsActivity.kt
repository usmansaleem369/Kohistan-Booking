package com.example.kohistanbookingapp.ui.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kohistanbookingapp.R
import com.example.kohistanbookingapp.api.ApiRequestStatus
import com.example.kohistanbookingapp.databinding.ActivityPassengerDetailsBinding
import com.example.kohistanbookingapp.databinding.DialogPaymentHoldBinding
import com.example.kohistanbookingapp.models.request.BookingSeatItem
import com.example.kohistanbookingapp.models.request.CreateBookingRequest
import com.example.kohistanbookingapp.models.response.SeatGender
import com.example.kohistanbookingapp.models.response.SelectedSeat
import com.example.kohistanbookingapp.util.AppLoader
import com.example.kohistanbookingapp.util.AppUtils
import com.example.kohistanbookingapp.util.AppUtils.applySystemAndImeInsets
import com.example.kohistanbookingapp.util.AppUtils.attachCnicFormatter
import com.example.kohistanbookingapp.util.AppUtils.clearErrorOnTextChange
import com.example.kohistanbookingapp.util.AppUtils.validateCnic
import com.example.kohistanbookingapp.util.AppUtils.validateEmail
import com.example.kohistanbookingapp.util.AppUtils.validatePakMobile
import com.example.kohistanbookingapp.util.JazzCashFailureMessageHandler.formatJazzCashFailureMessage
import com.example.kohistanbookingapp.util.SharedPreferencesHelper
import com.example.kohistanbookingapp.viewmodel.CreateBookingViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class PassengerDetailsActivity : AppCompatActivity() {

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var binding: ActivityPassengerDetailsBinding
    private val createBookingViewModel: CreateBookingViewModel by viewModels()

    private var selectedSeats: List<SelectedSeat> = emptyList()
    private var totalAmount: Double = 0.0
    private val currencyFormat = NumberFormat.getIntegerInstance(Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPassengerDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.main.applySystemAndImeInsets()

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
            if (validateInputs(isPayOnline = true)) {
                submitBookingRequest(isPayOnline = true)
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

        totalAmount = selectedSeats.size * (route?.getEffectiveFare() ?: 0.0)
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
        binding.llPassengerDetails.apply {
            etPassengerName.clearErrorOnTextChange()
            etPassengerEmail.clearErrorOnTextChange()
            etPassengerCnic.attachCnicFormatter(mirrorTarget = binding.llPaymentMethod.etJazzCashCnic)

            etPassengerPhone.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (etPassengerPhone.error != null) etPassengerPhone.error = null
                }
                override fun afterTextChanged(s: Editable?) {
                    val target = binding.llPaymentMethod.etJazzCashAccount
                    if (!target.hasFocus()) {
                        target.setText(s?.toString().orEmpty())
                    }
                }
            })
        }

        binding.llPaymentMethod.apply {
            etJazzCashAccount.clearErrorOnTextChange()
            etJazzCashCnic.attachCnicFormatter(mirrorTarget = etJazzCashCnic)
        }
    }


    private fun validateInputs(isPayOnline: Boolean): Boolean {
        if (selectedSeats.isEmpty()) {
            AppUtils.showCustomToast(
                "No seats selected. Please go back and select seats.",
                layoutInflater,
                this
            )
            return false
        }

        binding.llPassengerDetails.apply {
            val name = etPassengerName.text.toString().trim()
            val email = etPassengerEmail.text.toString().trim()

            if (name.length < 3) {
                etPassengerName.error = "Please enter valid passenger full name (at least 3 characters)"
                etPassengerName.requestFocus()
                return false
            }

            if (!etPassengerCnic.validateCnic()) return false

            if (!etPassengerPhone.validatePakMobile()) return false

            if (!etPassengerEmail.validateEmail()) return false
        }

        if (isPayOnline) {
            binding.llPaymentMethod.apply {
                if (!etJazzCashAccount.validatePakMobile("Enter a valid 11-digit JazzCash account number (03XXXXXXXXX)")) {
                    return false
                }
                if (!etJazzCashCnic.validateCnic("Please enter a valid 13-digit account holder CNIC")) {
                    return false
                }
            }
        }

        return true
    }


    private fun submitBookingRequest(isPayOnline: Boolean) {
        val route = sharedPreferencesHelper.selectedRouteData ?: run {
            AppUtils.showCustomToast("Route information missing. Please search again.", layoutInflater, this)
            return
        }
        val seatItems = selectedSeats.sortedBy { it.seatNumber }.map { seat ->
                BookingSeatItem(
                    seatNo = seat.seatNumber,
                    gender = if (seat.assignedGender == SeatGender.FEMALE) "Female" else "Male"
                )
            }

        val rawAccMobile = binding.llPaymentMethod.etJazzCashAccount.text.toString().replace("-", "").trim()
        val formattedAccMobile = if (rawAccMobile.length == 11) {
            "${rawAccMobile.substring(0, 4)}-${rawAccMobile.substring(4)}"
        } else {
            rawAccMobile
        }
        val formattedAccCnic = binding.llPaymentMethod.etJazzCashCnic.text.toString().trim()

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
            email = binding.llPassengerDetails.etPassengerEmail.text.toString().trim().ifEmpty { null },
            payOnline = isPayOnline,
            accCnic = if (isPayOnline) formattedAccCnic else null,
            accMobileNumber = if (isPayOnline) formattedAccMobile else null
        )

        createBookingViewModel.createBooking(request)
    }

    private fun observeViewModel() {
        createBookingViewModel.createBookingResponse.observe(this) { event ->
            event.getContentIfNotHandled()?.let { bookingData ->
                val payment = bookingData.payment
                val isOnlineUnpaid = payment != null && !payment.isPaid

                if (isOnlineUnpaid) {
                    val errorReason = bookingData.paymentMessage?.takeIf { it.isNotBlank() } ?: payment.responseMessage?.takeIf { it.isNotBlank() } ?: "JazzCash payment could not be completed."
                    val message = formatJazzCashFailureMessage(errorReason)
                    showPaymentHoldDialog(
                        reservationId = bookingData.reservationId.orEmpty(),
                        errorMessage = message
                    )
                } else {
                    AppUtils.showCustomToast(
                        "Seats reserved! Reservation ID: ${bookingData.reservationId}",
                        layoutInflater,
                        this
                    )
                    finishBookingAndNavigateHome()
                }
            }
        }

        createBookingViewModel.apiRequestStatus.observe(this) { event ->
            event.getContentIfNotHandled()?.let { status ->
                when (status) {
                    is ApiRequestStatus.Loading -> AppLoader.instance.showLoader(this)
                    is ApiRequestStatus.Idle -> AppLoader.instance.hideLoader()
                    is ApiRequestStatus.Error -> {
                        AppLoader.instance.hideLoader()
                        if (status.showDialog) {
                            AppUtils.showErrorDialog(this,  status.message, onOkClick = {
                                if (status.message.contains("My Bookings",ignoreCase = true)) {
                                    finishBookingAndNavigateHome()
                                }
                            })
                        } else {
                            AppUtils.showCustomToast(status.message, layoutInflater, this)
                        }
                    }
                }
            }
        }
    }

    private fun showPaymentHoldDialog(reservationId: String, errorMessage: String) {
        val dialogBinding = DialogPaymentHoldBinding.inflate(layoutInflater)
        val dialog = Dialog(this).apply {
            setContentView(dialogBinding.root)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        dialogBinding.tvPaymentError.text = errorMessage
        dialogBinding.tvReservationRef.text = getString(R.string.reservation_id, reservationId)

        dialogBinding.btnGoToHome.setOnClickListener {
            dialog.dismiss()
            finishBookingAndNavigateHome()
        }

        dialog.show()
    }

    private fun finishBookingAndNavigateHome() {
        sharedPreferencesHelper.clearTicketSession()
        createBookingViewModel.clearSession()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finishAffinity()
    }
}
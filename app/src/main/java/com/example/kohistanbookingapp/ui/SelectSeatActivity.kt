package com.example.kohistanbookingapp.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kohistanbookingapp.R
import com.example.kohistanbookingapp.adapters.BusSeatAdapter
import com.example.kohistanbookingapp.api.ApiRequestStatus
import com.example.kohistanbookingapp.databinding.ActivitySelectSeatBinding
import com.example.kohistanbookingapp.databinding.BottomSheetSelectGenderBinding
import com.example.kohistanbookingapp.models.response.BookedSeatsData
import com.example.kohistanbookingapp.models.response.SeatGender
import com.example.kohistanbookingapp.models.response.SeatPatternRow
import com.example.kohistanbookingapp.models.response.SelectedSeat
import com.example.kohistanbookingapp.util.AppLoader
import com.example.kohistanbookingapp.util.AppUtils
import com.example.kohistanbookingapp.util.SharedPreferencesHelper
import com.example.kohistanbookingapp.viewmodel.ApiViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class SelectSeatActivity : AppCompatActivity() {

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var binding: ActivitySelectSeatBinding
    private val apiViewModel: ApiViewModel by viewModels()
    private lateinit var seatAdapter: BusSeatAdapter

    private var cachedSeatPattern: List<SeatPatternRow>? = null
    private var cachedBookedSeatsData: BookedSeatsData? = null
    private val currencyFormat = NumberFormat.getIntegerInstance(Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySelectSeatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initUIAndListeners()
        setupSeatGridAdapter()
        observeViewModel()
        fetchSeatData()
    }

    private fun fetchSeatData() {
        val route = sharedPreferencesHelper.selectedRouteData ?: return
        val travelDate = sharedPreferencesHelper.selectedTravelDate.orEmpty()

        apiViewModel.getSeatPattern(route.vehicleTypeId ?: -1)

    }

    private fun initUIAndListeners() {
        sharedPreferencesHelper.selectedRouteData?.let {
            binding.tvRouteHeader.text = getString(R.string.to_from, it.fromCity, it.toCity)
            val formattedTime = AppUtils.formatToAmPm(it.routeTimeValue)
            binding.tvScheduleSubHeader.text = listOf(
                sharedPreferencesHelper.selectedTravelDate ?: "",
                formattedTime,
                it.vehicleType ?: ""
            )
                .filter { data -> data.isNotBlank() }
                .joinToString(" • ")
        }

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnProceed.setOnClickListener {
            val chosenSeats = seatAdapter.getSelectedSeats()
            if (chosenSeats.isEmpty()) return@setOnClickListener
            sharedPreferencesHelper.selectedSeats = seatAdapter.getSelectedSeats()
            val intent = Intent(this, PassengerDetailsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupSeatGridAdapter() {
        seatAdapter = BusSeatAdapter(
            maxSelection = 6,
            onRequestGenderSelection = { seatNumber ->
                showGenderSelectionBottomSheet(seatNumber)
            },
            onSelectionChanged = { selectedSeats ->
                renderDataOnSeatSelection(selectedSeats)
            },
            onSeatError = { message ->
                AppUtils.showCustomToast(message, layoutInflater, this)
            }
        )

        val nonScrollableGridLayoutManager = object : GridLayoutManager(this, 5) {
            override fun canScrollVertically(): Boolean = false
        }

        binding.rvBusSeats.apply {
            layoutManager = nonScrollableGridLayoutManager
            adapter = seatAdapter
            itemAnimator = null
            isNestedScrollingEnabled = false
            post {
                seatAdapter.setContainerHeight(height)
            }
        }
    }

    fun renderDataOnSeatSelection(selectedSeats: List<SelectedSeat>) {
        val count = selectedSeats.size
        val totalFare = count * (sharedPreferencesHelper.selectedRouteData?.fare ?: 0.0)

        binding.tvSelectedSeats.text = if (count > 0) {
            selectedSeats.sortedBy { it.seatNumber }.joinToString(", ") {
                val tag = if (it.assignedGender == SeatGender.FEMALE) "F" else "M"
                "${it.seatNumber}($tag)"
            }
        } else {
            "None"
        }
        binding.tvTotalFare.text = "PKR ${currencyFormat.format(totalFare)}"
        binding.btnProceed.isEnabled = count > 0
    }

    private fun showGenderSelectionBottomSheet(seatNumber: Int) {
        val selectGenderBottomSheet = BottomSheetDialog(this)
        val selectGenderBottomSheetBinding = BottomSheetSelectGenderBinding.inflate(layoutInflater)

        selectGenderBottomSheetBinding.tvSheetTitle.text =
            getString(R.string.seat_passenger_gender, seatNumber)
        selectGenderBottomSheetBinding.cardMale.setOnClickListener {
            selectGenderBottomSheet.dismiss()
            seatAdapter.confirmSeatSelection(seatNumber, SeatGender.MALE)
        }
        selectGenderBottomSheetBinding.cardFemale.setOnClickListener {
            selectGenderBottomSheet.dismiss()
            seatAdapter.confirmSeatSelection(seatNumber, SeatGender.FEMALE)
        }

        selectGenderBottomSheet.setContentView(selectGenderBottomSheetBinding.root)

        selectGenderBottomSheet.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet =
                d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundColor(Color.TRANSPARENT)
        }

        selectGenderBottomSheet.show()
    }

    private fun observeViewModel() {
        apiViewModel.seatPatternResponse.observe(this) { event ->
            event.getContentIfNotHandled()?.let { seatPatternRows ->

                val vehicleId = seatPatternRows.firstOrNull()?.vehicleId
                cachedSeatPattern = seatPatternRows.filter {
                    it.vehicleId == vehicleId
                }
               // renderBusSeatsIfReady()
                apiViewModel.getBookedSeats()
            }
        }

        apiViewModel.bookedSeatsResponse.observe(this) { event ->
            event.getContentIfNotHandled()?.let { bookedSeatsData ->
                cachedBookedSeatsData = bookedSeatsData
                renderBusSeatsIfReady()
            }
        }

        apiViewModel.apiRequestStatus.observe(this) { event ->
            event.getContentIfNotHandled()?.let { status ->
                handleApiStatus(status)
            }
        }
    }

    private fun renderBusSeatsIfReady() {
        val rows = cachedSeatPattern ?: return
        val bookedData = cachedBookedSeatsData

        val femaleBookedSeats = bookedData?.getFemaleBookedSeatSet() ?: emptySet()
        val maleBookedSeats = bookedData?.getMaleBookedSeatSet() ?: emptySet()
        val genericDisabled = sharedPreferencesHelper.selectedRouteData
            ?.getDisabledSeatSet()
            ?.filterNot { femaleBookedSeats.contains(it) }
            ?.toSet()
            ?: emptySet()

        seatAdapter.submitSeatPattern(
            rows = rows,
            maleBookedSeats = maleBookedSeats + genericDisabled,
            femaleBookedSeats = femaleBookedSeats
        )
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
}
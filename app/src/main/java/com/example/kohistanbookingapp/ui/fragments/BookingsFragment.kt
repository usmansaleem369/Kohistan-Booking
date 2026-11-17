package com.example.kohistanbookingapp.ui.fragments

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kohistanbookingapp.R
import com.example.kohistanbookingapp.adapters.BookingHistoryAdapter
import com.example.kohistanbookingapp.adapters.CancelSeatAdapter
import com.example.kohistanbookingapp.api.ApiRequestStatus
import com.example.kohistanbookingapp.databinding.DialogConfirmCancelBinding
import com.example.kohistanbookingapp.databinding.FragmentBookingsBinding
import com.example.kohistanbookingapp.models.local.BookingFilter
import com.example.kohistanbookingapp.models.response.BookingHistoryItem
import com.example.kohistanbookingapp.util.AppLoader
import com.example.kohistanbookingapp.util.AppUtils
import com.example.kohistanbookingapp.util.SharedPreferencesHelper
import com.example.kohistanbookingapp.viewmodel.BookingHistoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BookingsFragment : Fragment() {

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    private var _binding: FragmentBookingsBinding? = null
    private val binding get() = _binding!!

    private val bookingHistoryViewModel: BookingHistoryViewModel by viewModels()
    private lateinit var bookingAdapter: BookingHistoryAdapter

    private val memberId: Int
        get() = sharedPreferencesHelper.userProfile?.memberId ?: 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUiAndListeners()
        observeViewModel()

        if (memberId != 0) {
            bookingHistoryViewModel.loadInitialBookingsIfNeeded(memberId)
        }
    }


    private fun updateTabStyles(selectedFilter: BookingFilter) {
        val tabs = listOf(
            binding.tabActive to BookingFilter.UPCOMING,
            binding.tabPast to BookingFilter.PAST,
            binding.tabCancelled to BookingFilter.CANCELLED
        )

        tabs.forEach { (tabView, filter) ->
            val isSelected = filter == selectedFilter
            if (isSelected) {
                tabView.setBackgroundResource(R.drawable.bg_filter_selected)
                tabView.setTextColor(Color.WHITE)
                tabView.typeface = ResourcesCompat.getFont(requireContext(), R.font.manrope_bold)
            } else {
                tabView.setBackgroundResource(R.drawable.bg_filter_unselected)
                tabView.setTextColor(0xFF667085.toInt())
                tabView.typeface =
                    ResourcesCompat.getFont(requireContext(), R.font.manrope_semibold)
            }
        }

        when (selectedFilter) {
            BookingFilter.UPCOMING -> {
                binding.tvEmptyTitle.text = getString(R.string.no_active_bookings)
                binding.tvEmptySubtitle.text =
                    getString(R.string.your_upcoming_and_active_bus_ticket_bookings_will_appear_here)
            }

            BookingFilter.PAST -> {
                binding.tvEmptyTitle.text = getString(R.string.no_past_bookings)
                binding.tvEmptySubtitle.text =
                    getString(R.string.your_completed_bus_journeys_will_appear_here)
            }

            BookingFilter.CANCELLED -> {
                binding.tvEmptyTitle.text = getString(R.string.no_cancelled_bookings)
                binding.tvEmptySubtitle.text =
                    getString(R.string.any_cancelled_reservations_will_appear_here)
            }
        }
    }

    private fun initUiAndListeners() {
        binding.swipeRefreshBookings.setProgressViewOffset(false, -1000, -1000)
        binding.swipeRefreshBookings.setOnRefreshListener {
            if (memberId != 0) {
                bookingHistoryViewModel.getBookingHistory(
                    memberId = memberId,
                    pageNumber = 1,
                    isRefresh = true
                )
            } else {
                binding.swipeRefreshBookings.isRefreshing = false
            }
        }

        binding.tabActive.setOnClickListener {
            bookingHistoryViewModel.selectFilter(BookingFilter.UPCOMING, memberId)
        }
        binding.tabPast.setOnClickListener {
            bookingHistoryViewModel.selectFilter(BookingFilter.PAST, memberId)
        }
        binding.tabCancelled.setOnClickListener {
            bookingHistoryViewModel.selectFilter(BookingFilter.CANCELLED, memberId)
        }

        bookingAdapter = BookingHistoryAdapter { bookingItem ->
            showCancelConfirmationDialog(bookingItem)
        }

        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.rvBookings.apply {
            layoutManager = linearLayoutManager
            adapter = bookingAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy <= 0 || memberId == 0) return

                    val totalItemCount = linearLayoutManager.itemCount
                    val lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()

                    if (lastVisibleItem >= totalItemCount - 2) {
                        bookingHistoryViewModel.loadNextBookingsPage(memberId)
                    }
                }
            })
        }
    }

    private fun showCancelConfirmationDialog(item: BookingHistoryItem) {
        val dialogBinding = DialogConfirmCancelBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext()).apply {
            setContentView(dialogBinding.root)
            setCancelable(true)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val allSeatNumbers = item.seats.joinToString(", ") { it.seatNo.toString() }
        dialogBinding.tvDialogTitle.text = getString(R.string.cancel_ticket)
        dialogBinding.tvDialogMessage.text = getString(
            R.string.cancel_reservation_detail,
            item.reservationId,
            item.fromCity,
            item.toCity,
            allSeatNumbers
        )

        val seatAdapter = CancelSeatAdapter(item.seats) { selected ->
            val hasSelection = selected.isNotEmpty()
            dialogBinding.btnConfirmCancel.isEnabled = hasSelection
            dialogBinding.btnConfirmCancel.alpha = if (hasSelection) 1f else 0.5f
            dialogBinding.tvSelectAllSeats.text = if (selected.size == item.seats.size) "Clear All" else "Select All"
        }
        dialogBinding.tvSelectAllSeats.setOnClickListener {
            seatAdapter.toggleSelectAll()
        }

        val spanCount = if (item.seats.size >= 3){
            3
        } else{
            item.seats.size
        }

        dialogBinding.rvCancelSeats.apply {
            layoutManager = GridLayoutManager(requireContext(), spanCount)
            adapter = seatAdapter
        }

        dialogBinding.btnKeepTicket.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnConfirmCancel.setOnClickListener {
            val selected = seatAdapter.getSelectedSeats()
            if (selected.isEmpty()) return@setOnClickListener
            dialog.dismiss()
            bookingHistoryViewModel.cancelBooking(
                bookingItem = item.copy(
                    seats = selected,
                    totalFare = selected.sumOf { it.fare ?:0.0 }
                )
            )
        }

        dialog.show()
    }

    private fun observeViewModel() {
        bookingHistoryViewModel.selectedFilter.observe(viewLifecycleOwner) { filter ->
            updateTabStyles(filter)
            bookingAdapter.setCanCancelTickets(filter == BookingFilter.UPCOMING)
        }

        bookingHistoryViewModel.bookingsList.observe(viewLifecycleOwner) { list ->
            bookingAdapter.submitList(list)
            val isEmpty = list.isEmpty()
            binding.llEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.rvBookings.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }

        bookingHistoryViewModel.isLoadingMore.observe(viewLifecycleOwner) { loadingMore ->
            binding.pbLoadMore.visibility = if (loadingMore) View.VISIBLE else View.GONE
        }

        bookingHistoryViewModel.bookingHistoryResponse.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { response ->
                binding.swipeRefreshBookings.isRefreshing = false
                bookingHistoryViewModel.onBookingHistoryFetched(response)
            }
        }

        bookingHistoryViewModel.cancelBookingResponse.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { response ->
                AppUtils.showCustomToast(response.message.ifEmpty { "Ticket cancelled successfully" }, layoutInflater, requireContext())
                bookingHistoryViewModel.removeCancelledBookingFromList()
            }
        }

        bookingHistoryViewModel.apiRequestStatus.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { status ->
                when (status) {
                    is ApiRequestStatus.Loading -> {
                        if (!binding.swipeRefreshBookings.isRefreshing &&
                            bookingHistoryViewModel.isLoadingMore.value != true
                        ) {
                            AppLoader.instance.showLoader(requireContext())
                        }
                    }
                    is ApiRequestStatus.Idle -> {
                        AppLoader.instance.hideLoader()
                        binding.swipeRefreshBookings.isRefreshing = false
                    }
                    is ApiRequestStatus.Error -> {
                        AppLoader.instance.hideLoader()
                        binding.swipeRefreshBookings.isRefreshing = false
                        bookingHistoryViewModel.onBookingHistoryError()
                        AppUtils.showCustomToast(status.message, layoutInflater, requireContext())
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.example.kohistanbookingapp.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kohistanbookingapp.R
import com.example.kohistanbookingapp.adapters.BookingHistoryAdapter
import com.example.kohistanbookingapp.api.ApiRequestStatus
import com.example.kohistanbookingapp.databinding.FragmentBookingsBinding
import com.example.kohistanbookingapp.models.local.BookingFilter
import com.example.kohistanbookingapp.util.AppLoader
import com.example.kohistanbookingapp.util.AppUtils
import com.example.kohistanbookingapp.util.SharedPreferencesHelper
import com.example.kohistanbookingapp.viewmodel.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BookingsFragment : Fragment() {

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    private var _binding: FragmentBookingsBinding? = null
    private val binding get() = _binding!!

    private val apiViewModel: ApiViewModel by viewModels()
    private lateinit var bookingAdapter: BookingHistoryAdapter

    private val memberId: Int
        get() = sharedPreferencesHelper.userProfile?.memberId ?: 0

    private val cnic: String
        get() = sharedPreferencesHelper.userProfile?.cnic ?: ""

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
            apiViewModel.loadInitialBookingsIfNeeded(memberId, cnic)
        }
    }


    private fun updateTabStyles(selectedFilter: BookingFilter) {
        val tabs = listOf(
            binding.tabActive to BookingFilter.ACTIVE,
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
                //tabView.setBackgroundColor(Color.TRANSPARENT)
                tabView.setBackgroundResource(R.drawable.bg_filter_unselected)
                tabView.setTextColor(0xFF667085.toInt())
                tabView.typeface = ResourcesCompat.getFont(requireContext(), R.font.manrope_semibold)
            }
        }

        when (selectedFilter) {
            BookingFilter.ACTIVE -> {
                binding.tvEmptyTitle.text = getString(R.string.no_active_bookings)
                binding.tvEmptySubtitle.text = getString(R.string.your_upcoming_and_active_bus_ticket_bookings_will_appear_here)
            }
            BookingFilter.PAST -> {
                binding.tvEmptyTitle.text = getString(R.string.no_past_bookings)
                binding.tvEmptySubtitle.text = getString(R.string.your_completed_bus_journeys_will_appear_here)
            }
            BookingFilter.CANCELLED -> {
                binding.tvEmptyTitle.text = getString(R.string.no_cancelled_bookings)
                binding.tvEmptySubtitle.text = getString(R.string.any_cancelled_reservations_will_appear_here)
            }
        }
    }

    private fun initUiAndListeners() {
        binding.swipeRefreshBookings.setProgressViewOffset(false, -1000, -1000)
        binding.swipeRefreshBookings.setOnRefreshListener {
            if (memberId != 0) {
                apiViewModel.getBookingHistory(
                    memberId = memberId,
                    cnic = cnic,
                    pageNumber = 1,
                    isRefresh = true
                )
            } else {
                binding.swipeRefreshBookings.isRefreshing = false
            }
        }

        binding.tabActive.setOnClickListener {
            apiViewModel.selectFilter(BookingFilter.ACTIVE, memberId, cnic)
        }
        binding.tabPast.setOnClickListener {
            apiViewModel.selectFilter(BookingFilter.PAST, memberId, cnic)
        }
        binding.tabCancelled.setOnClickListener {
            apiViewModel.selectFilter(BookingFilter.CANCELLED, memberId, cnic)
        }

        bookingAdapter = BookingHistoryAdapter { bookingItem ->
            AppUtils.showCustomToast(
                "Reservation #${bookingItem.reservationId}",
                layoutInflater,
                requireContext()
            )
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
                        apiViewModel.loadNextBookingsPage(memberId, cnic)
                    }
                }
            })
        }
    }

    private fun observeViewModel() {
        apiViewModel.selectedFilter.observe(viewLifecycleOwner) { filter ->
            updateTabStyles(filter)
        }

        apiViewModel.bookingsList.observe(viewLifecycleOwner) { list ->
            bookingAdapter.submitList(list)
            val isEmpty = list.isEmpty()
            binding.llEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.rvBookings.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }

        apiViewModel.isLoadingMore.observe(viewLifecycleOwner) { loadingMore ->
            binding.pbLoadMore.visibility = if (loadingMore) View.VISIBLE else View.GONE
        }

        apiViewModel.bookingHistoryResponse.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { response ->
                binding.swipeRefreshBookings.isRefreshing = false
                apiViewModel.onBookingHistoryFetched(response)
            }
        }

        apiViewModel.apiRequestStatus.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { status ->
                when (status) {
                    is ApiRequestStatus.Loading -> {
                        if (!binding.swipeRefreshBookings.isRefreshing &&
                            apiViewModel.isLoadingMore.value != true
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
                        apiViewModel.onBookingHistoryError()
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
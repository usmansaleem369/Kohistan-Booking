package com.example.kohistanbookingapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kohistanbookingapp.R
import com.example.kohistanbookingapp.adapters.BookingHistoryAdapter
import com.example.kohistanbookingapp.api.ApiRequestStatus
import com.example.kohistanbookingapp.databinding.FragmentBookingsBinding
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

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        if (memberId != 0) {
            apiViewModel.loadInitialBookingsIfNeeded(memberId)
        }
    }

    private fun setupRecyclerView() {
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
                        apiViewModel.loadNextBookingsPage(memberId)
                    }
                }
            })
        }
    }

    private fun setupListeners() {
        binding.swipeRefreshBookings.setColorSchemeResources(R.color.btn_navy)
        binding.swipeRefreshBookings.setOnRefreshListener {
            if (memberId != 0) {
                apiViewModel.getBookingHistory(memberId = memberId, pageNumber = 1, isRefresh = true)
            } else {
                binding.swipeRefreshBookings.isRefreshing = false
            }
        }
    }

    private fun observeViewModel() {
        apiViewModel.bookingsList.observe(viewLifecycleOwner) { list ->
            bookingAdapter.submitList(list)
            val isEmpty = list.isEmpty()
            binding.llEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.rvBookings.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }

        apiViewModel.totalRecords.observe(viewLifecycleOwner) { total ->
            if (total > 0) {
                binding.tvTotalBookingsBadge.visibility = View.VISIBLE
                binding.tvTotalBookingsBadge.text =
                    if (total == 1) "1 Booking" else "$total Bookings"
            } else {
                binding.tvTotalBookingsBadge.visibility = View.GONE
            }
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
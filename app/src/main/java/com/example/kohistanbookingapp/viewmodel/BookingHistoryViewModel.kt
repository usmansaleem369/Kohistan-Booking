package com.example.kohistanbookingapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.kohistanbookingapp.models.local.BookingFilter
import com.example.kohistanbookingapp.models.request.CancelBookingRequest
import com.example.kohistanbookingapp.models.request.JazzCashPayRequest
import com.example.kohistanbookingapp.models.response.BookingHistoryItem
import com.example.kohistanbookingapp.models.response.BookingHistoryResponse
import com.example.kohistanbookingapp.repository.BookingHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingHistoryViewModel@Inject constructor(
    application: Application,
    private val bookingHistoryRepository: BookingHistoryRepository
) : AndroidViewModel(application) {
    val apiRequestStatus = bookingHistoryRepository.apiRequestStatus
    val bookingHistoryResponse = bookingHistoryRepository.bookingHistoryResponse

    private val _bookingsList = MutableLiveData<List<BookingHistoryItem>>(emptyList())
    val bookingsList: LiveData<List<BookingHistoryItem>> = _bookingsList

    val cancelBookingResponse = bookingHistoryRepository.cancelBookingResponse

    val payAfterBookingResponse = bookingHistoryRepository.payAfterBookingResponse

    private val _isLoadingMore = MutableLiveData(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private val _selectedFilter = MutableLiveData(BookingFilter.UPCOMING)
    val selectedFilter: LiveData<BookingFilter> = _selectedFilter

    private var currentPage = 0
    private var totalPages = 1
    private val pageSize = 10

    var pendingCancelTransactionId: Long = -1
    private var pendingCancelledSeatNos: List<Int> = emptyList()

    fun loadInitialBookingsIfNeeded(memberId: Int) {
        if (currentPage == 0) {
            getBookingHistory(memberId = memberId, pageNumber = 1, isRefresh = true)
        }
    }

    fun loadNextBookingsPage(memberId: Int) {
        if (_isLoadingMore.value == true || currentPage >= totalPages) return
        _isLoadingMore.value = true
        getBookingHistory( memberId = memberId, pageNumber = currentPage + 1, isRefresh = false)
    }

    fun selectFilter(filter: BookingFilter, memberId: Int) {
        if (_selectedFilter.value == filter) return
        _selectedFilter.value = filter
        currentPage = 0
        totalPages = 1
        _bookingsList.value = emptyList()
        _isLoadingMore.value = false

        if (memberId != 0) {
            getBookingHistory(memberId = memberId, pageNumber = 1, isRefresh = true)
        }
    }

    fun getBookingHistory(memberId: Int, pageNumber: Int = 1, isRefresh: Boolean = pageNumber == 1) {
        if (!isRefresh) {
            _isLoadingMore.value = true
        }
        val currentFilter = _selectedFilter.value?.label ?: BookingFilter.UPCOMING.label
        viewModelScope.launch {
            bookingHistoryRepository.getBookingHistory(
                memberId = memberId,
                pageNumber = pageNumber,
                pageSize = pageSize,
                status = currentFilter
            )
        }
    }

    fun onBookingHistoryFetched(response: BookingHistoryResponse) {
        _isLoadingMore.value = false

        val meta = response.meta
        currentPage = meta?.pageNumber ?: 1
        totalPages = meta?.totalPages ?: 1

        val currentItems = if (currentPage == 1) emptyList() else _bookingsList.value.orEmpty()
        val merged = (currentItems + response.data).distinctBy { it.transactionId }

        _bookingsList.value = merged
    }

    fun onBookingHistoryError() {
        _isLoadingMore.value = false
    }

    fun cancelBooking(bookingItem: BookingHistoryItem) {
        pendingCancelTransactionId = bookingItem.transactionId
        pendingCancelledSeatNos = bookingItem.seats.map { it.seatNo }

        val cancelBookingRequest = CancelBookingRequest(transactionId = pendingCancelTransactionId, seatNos = pendingCancelledSeatNos)
        viewModelScope.launch {
            bookingHistoryRepository.cancelBooking(
              cancelBookingRequest
            )

        }
    }

    fun removeCancelledBookingFromList() {
        if (pendingCancelTransactionId == -1L || pendingCancelledSeatNos.isEmpty()) return


        val updated = _bookingsList.value.orEmpty().mapNotNull { bookingHistoryItem ->
            if (bookingHistoryItem.transactionId != pendingCancelTransactionId) {
                bookingHistoryItem
            } else {
                val remainingSeats = bookingHistoryItem.seats.filterNot { it.seatNo in pendingCancelledSeatNos }
                if (remainingSeats.isEmpty()) {
                    null
                } else {
                    bookingHistoryItem.copy(
                        seats = remainingSeats,
                        totalFare = remainingSeats.sumOf { it.fare?:0.0 }
                    )
                }
            }
        }

        _bookingsList.postValue(updated)



        pendingCancelTransactionId = -1L
        pendingCancelledSeatNos = emptyList()
    }

    fun payAfterBooking(jazzCashPayRequest: JazzCashPayRequest) {
        viewModelScope.launch {
            bookingHistoryRepository.payAfterBookingWithWorker(
                jazzCashPayRequest
            )

        }
    }

    fun markBookingAsPaid(bookingTransactionId: Long) {
        val updated = _bookingsList.value.orEmpty().map { bookingHistoryItem ->
            if (bookingHistoryItem.transactionId == bookingTransactionId) {
                bookingHistoryItem.copy(isPaid = true) // match the paid flag field name in BookingHistoryItem
            } else {
                bookingHistoryItem
            }
        }
        _bookingsList.value = updated
    }



    fun clearSession() {
        currentPage = 0
        totalPages = 1
        _selectedFilter.value = BookingFilter.UPCOMING
        _bookingsList.value = emptyList()
        _isLoadingMore.value = false
    }

}
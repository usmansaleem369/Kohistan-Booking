package com.example.kohistanbookingapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kohistanbookingapp.R
import com.example.kohistanbookingapp.databinding.ItemBookingHistoryBinding
import com.example.kohistanbookingapp.models.response.BookingHistoryItem
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class BookingHistoryAdapter(
    private val onCancelClick: (BookingHistoryItem) -> Unit,
    private val onPayNowClick: (BookingHistoryItem) -> Unit
) : ListAdapter<BookingHistoryItem, BookingHistoryAdapter.BookingViewHolder>(DiffCallback) {

    private var canCancelTickets: Boolean = true

    fun setCanCancelTickets(canCancel: Boolean) {
        if (canCancelTickets != canCancel) {
            canCancelTickets = canCancel
            notifyDataSetChanged()
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookingViewHolder(
        private val binding: ItemBookingHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(bookingHistoryItem: BookingHistoryItem) {
            binding.apply {
                tvReservationId.text =
                    root.context.getString(R.string.booking_id, bookingHistoryItem.reservationId)
                tvBookedAt.text =
                    root.context.getString(R.string.booked_on, formatBookedAt(bookingHistoryItem.bookedAt))
                tvTimeType.text = bookingHistoryItem.timeType

                tvFromCity.text = bookingHistoryItem.fromCity
                tvToCity.text = bookingHistoryItem.toCity
                // tvRouteName.text = item.routeName
                // tvDropPoint.text = "Drop: ${item.dropPoint}"

                val formattedDate = formatDepartureDate(bookingHistoryItem.departureDate)
                val formattedTime = formatDepartureTime(bookingHistoryItem.departureTime)
                tvDepartureDateTime.text = "$formattedDate • $formattedTime"

                val seatCount = bookingHistoryItem.seats.size
                tvSeatCountLabel.text = if (seatCount == 1) "Seat (1)" else "Seats ($seatCount)"
                tvSeatNumbers.text = bookingHistoryItem.seats.joinToString(", ") { seat ->
                    val g = seat.gender?.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
                    if (g.isNotEmpty()) "${seat.seatNo} ($g)" else "${seat.seatNo}"
                }

                tvPassengerName.text = bookingHistoryItem.passengerName
                tvContactNo.text = bookingHistoryItem.contactNo
                tvTotalFare.text = formatCurrency(bookingHistoryItem.totalFare)

                // root.setOnClickListener { onItemClick(item) }

                val showActions = canCancelTickets
                llBookingActions.visibility = if (showActions) View.VISIBLE else View.GONE
                btnCancelTicket.visibility = if (showActions) View.VISIBLE else View.GONE
                btnPayNow.isVisible = !bookingHistoryItem.isPaid

                btnCancelTicket.setOnClickListener { onCancelClick(bookingHistoryItem) }
                btnPayNow.setOnClickListener { onPayNowClick(bookingHistoryItem) }
            }
        }

        private fun formatDepartureDate(raw: String): String {
            return runCatching {
                val clean = raw.substringBefore("T")
                val parser = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                val formatter = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
                parser.parse(clean)?.let { formatter.format(it) } ?: clean
            }.getOrDefault(raw.substringBefore("T"))
        }

        private fun formatDepartureTime(raw: String): String {
            return runCatching {
                val parser = SimpleDateFormat("HH:mm", Locale.ENGLISH)
                val formatter = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
                parser.parse(raw)?.let { formatter.format(it) } ?: raw
            }.getOrDefault(raw)
        }

        private fun formatBookedAt(raw: String): String {
            return runCatching {
                val clean = raw.substringBefore(".")
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
                val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
                parser.parse(clean)?.let { formatter.format(it) } ?: raw.substringBefore("T")
            }.getOrDefault(raw.substringBefore("T"))
        }

        private fun formatCurrency(amount: Double): String {
            val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
                maximumFractionDigits = 0
            }
            return "PKR. ${formatter.format(amount)}"
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<BookingHistoryItem>() {
        override fun areItemsTheSame(
            oldItem: BookingHistoryItem,
            newItem: BookingHistoryItem
        ): Boolean = oldItem.transactionId == newItem.transactionId

        override fun areContentsTheSame(
            oldItem: BookingHistoryItem,
            newItem: BookingHistoryItem
        ): Boolean = oldItem == newItem
    }
}
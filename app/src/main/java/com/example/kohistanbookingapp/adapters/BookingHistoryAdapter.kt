package com.example.kohistanbookingapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kohistanbookingapp.databinding.ItemBookingHistoryBinding
import com.example.kohistanbookingapp.models.response.BookingHistoryItem
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class BookingHistoryAdapter(
    private val onItemClick: (BookingHistoryItem) -> Unit
) : ListAdapter<BookingHistoryItem, BookingHistoryAdapter.BookingViewHolder>(DiffCallback) {

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

        fun bind(item: BookingHistoryItem) {
            binding.tvReservationId.text = "ID #${item.reservationId}"
            binding.tvBookedAt.text = "Booked on ${formatBookedAt(item.bookedAt)}"
            binding.tvTimeType.text = item.timeType

            binding.tvFromCity.text = item.fromCity
            binding.tvToCity.text = item.toCity
            binding.tvRouteName.text = item.routeName
            binding.tvDropPoint.text = "Drop: ${item.dropPoint}"

            val formattedDate = formatDepartureDate(item.departureDate)
            val formattedTime = formatDepartureTime(item.departureTime)
            binding.tvDepartureDateTime.text = "$formattedDate • $formattedTime"

            val seatCount = item.seats.size
            binding.tvSeatCountLabel.text = if (seatCount == 1) "Seat (1)" else "Seats ($seatCount)"
            binding.tvSeatNumbers.text = item.seats.joinToString(", ") { seat ->
                val g = seat.gender?.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
                if (g.isNotEmpty()) "${seat.seatNo} ($g)" else "${seat.seatNo}"
            }

            binding.tvPassengerName.text = item.passengerName
            binding.tvContactNo.text = item.contactNo
            binding.tvTotalFare.text = formatCurrency(item.totalFare)

            binding.root.setOnClickListener { onItemClick(item) }
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
            return "Rs. ${formatter.format(amount)}"
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
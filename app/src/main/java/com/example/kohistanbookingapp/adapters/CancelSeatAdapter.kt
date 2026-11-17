package com.example.kohistanbookingapp.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.kohistanbookingapp.R
import com.example.kohistanbookingapp.databinding.ItemBusSeatBinding
import com.example.kohistanbookingapp.models.response.BookedSeatItem

class CancelSeatAdapter(
    private val seats: List<BookedSeatItem>,
    private val onSelectionChanged: (selectedSeats: List<BookedSeatItem>) -> Unit
) : RecyclerView.Adapter<CancelSeatAdapter.SeatViewHolder>() {

    private val selectedSeatNos = seats.map { it.seatNo }.toMutableSet()

    fun getSelectedSeats(): List<BookedSeatItem> =
        seats.filter { it.seatNo in selectedSeatNos }

    fun toggleSelectAll() {
        if (selectedSeatNos.size == seats.size) {
            selectedSeatNos.clear()
        } else {
            selectedSeatNos.addAll(seats.map { it.seatNo })
        }
        notifyDataSetChanged()
        onSelectionChanged(getSelectedSeats())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatViewHolder {
        val binding = ItemBusSeatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SeatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SeatViewHolder, position: Int) {
        holder.bind(seats[position])
    }

    override fun getItemCount(): Int = seats.size

    inner class SeatViewHolder(
        private val binding: ItemBusSeatBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(seat: BookedSeatItem) {
            val isSelected = seat.seatNo in selectedSeatNos
            val genderTag = seat.gender?.firstOrNull()?.uppercaseChar()?.let { " ($it)" }.orEmpty()

            binding.tvSeatNumber.text = "Seat ${seat.seatNo}$genderTag"
            binding.tvSeatNumber.isSelected = isSelected
            if (isSelected) {
                binding.tvSeatNumber.setBackgroundResource(R.drawable.bg_seat_selected)
                binding.tvSeatNumber.setTextColor(Color.WHITE)
            } else {
                binding.tvSeatNumber.setBackgroundResource(R.drawable.bg_seat_available) // or your unselected drawable
                binding.tvSeatNumber.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.btn_navy)
                )
            }

            binding.tvSeatNumber.setOnClickListener {
                if (seat.seatNo in selectedSeatNos) {
                    selectedSeatNos.remove(seat.seatNo)
                } else {
                    selectedSeatNos.add(seat.seatNo)
                }
                notifyItemChanged(bindingAdapterPosition)
                onSelectionChanged(getSelectedSeats())
            }
        }
    }
}
package com.example.kohistanbookingapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kohistanbookingapp.databinding.ItemRouteTimeBinding
import com.example.kohistanbookingapp.models.response.RouteData
import com.example.kohistanbookingapp.util.AppUtils
import com.example.kohistanbookingapp.util.TravelTimeEstimator
import java.text.NumberFormat
import java.util.Locale

class RouteTimeAdapter(
    private val onTimeSelected: (RouteData) -> Unit
) : ListAdapter<RouteData, RouteTimeAdapter.RouteTimeViewHolder>(DiffCallback) {

    private var bookingDate: String = ""

    fun submitRoutes(newList: List<RouteData>?, date: String) {
        val dateChanged = bookingDate != date
        bookingDate = date

        submitList(newList) {
            if (dateChanged && itemCount > 0) {
                notifyItemRangeChanged(0, itemCount, PAYLOAD_BOOKING_DATE)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteTimeViewHolder {
        val binding = ItemRouteTimeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RouteTimeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RouteTimeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: RouteTimeViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty() && payloads.all { it == PAYLOAD_BOOKING_DATE }) {
            holder.bindDate(bookingDate)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    inner class RouteTimeViewHolder(
        private val binding: ItemRouteTimeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            val clickListener = {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTimeSelected(getItem(position))
                }
            }
            binding.btnConfirm.setOnClickListener { clickListener() }
            binding.root.setOnClickListener { clickListener() }
        }

        fun bind(item: RouteData) {
            binding.apply {
                tvDepartureTime.text = AppUtils.formatToAmPm(item.routeTimeValue.orEmpty())
                tvType.text = item.routeTimeType ?: "Regular"
                tvPrice.text = item.fare?.let { "PKR $it" } ?: "N/A"
                tvFromCity.text = item.fromCity.orEmpty()
                tvToCity.text = item.toCity.orEmpty()
                tvRemainingSeats.text = "${item.remainingSeats} Seat Left"
                tvArrival.text = TravelTimeEstimator.calculateArrivalTime(
                    departureTime24 = item.routeTimeValue,
                    fromCity = item.fromCity,
                    toCity = item.toCity
                )
                bindDate(bookingDate)
            }
        }

        fun bindDate(date: String) {
            binding.tvBookingDate.text = date
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<RouteData>() {
        private const val PAYLOAD_BOOKING_DATE = "PAYLOAD_BOOKING_DATE"

        override fun areItemsTheSame(oldItem: RouteData, newItem: RouteData): Boolean {
            return oldItem.routeTimeId == newItem.routeTimeId
        }

        override fun areContentsTheSame(oldItem: RouteData, newItem: RouteData): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: RouteData, newItem: RouteData): Any? {
            // Return a bundle/list of changed fields if needed for partial rebinds
            return super.getChangePayload(oldItem, newItem)
        }
    }
}
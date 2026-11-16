package com.example.kohistanbookingapp.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
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
    private val priceFormatter = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 0
    }

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
            val itemClickListener = {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTimeSelected(getItem(position))
                }
            }
            binding.btnConfirm.setOnClickListener { itemClickListener() }
            binding.root.setOnClickListener { itemClickListener() }
        }

        fun bind(item: RouteData) {
            binding.apply {
                tvDepartureTime.text = AppUtils.formatToAmPm(item.routeTimeValue.orEmpty())
                tvType.text = item.vehicleType ?: "Economical"
                tvFromCity.text = item.fromCity.orEmpty()
                tvToCity.text = item.toCity.orEmpty()

                val seats = item.remainingSeats ?: 0
                tvRemainingSeats.text = if (seats == 1) "1 Seat Left" else "$seats Seats Left"

                tvArrival.text = TravelTimeEstimator.calculateArrivalTime(
                    departureTime24 = item.routeTimeValue,
                    fromCity = item.fromCity,
                    toCity = item.toCity
                )

                bindPricingAndPromo(item)
                bindDate(bookingDate)
            }
        }

        private fun bindPricingAndPromo(item: RouteData) {
            binding.apply {
                val effectiveFare = item.getEffectiveFare()
                tvPrice.text = if (effectiveFare > 0.0) {
                    "PKR ${priceFormatter.format(effectiveFare)}"
                } else {
                    "N/A"
                }

                if (item.hasPromotion() && item.fare != null) {
                    tvOriginalPrice.visibility = View.VISIBLE
                    tvOriginalPrice.text = "PKR ${priceFormatter.format(item.fare)}"
                    tvOriginalPrice.paintFlags = tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                    val discountPct = item.discountPercentage?.toInt() ?: 0
                    val promoTitle = item.promotionName.orEmpty().trim()
                    val badgeText = buildString {
                        if (promoTitle.isNotEmpty()) append(promoTitle)
                        if (promoTitle.isNotEmpty() && discountPct > 0) append(" • ")
                        if (discountPct > 0) append("$discountPct% OFF")
                    }

                    if (badgeText.isNotEmpty()) {
                        tvPromoBadge.visibility = View.VISIBLE
                        tvPromoBadge.text = badgeText
                    } else {
                        tvPromoBadge.visibility = View.GONE
                    }
                } else {
                    tvOriginalPrice.visibility = View.GONE
                    tvOriginalPrice.paintFlags =
                        tvOriginalPrice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    tvPromoBadge.visibility = View.GONE
                }
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
            return super.getChangePayload(oldItem, newItem)
        }
    }
}
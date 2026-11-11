package com.example.kohistanbookingapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kohistanbookingapp.databinding.ItemRouteTimeBinding
import com.example.kohistanbookingapp.models.response.RouteTime
import com.example.kohistanbookingapp.util.AppUtils

class RouteTimeAdapter(
    private var fromCity: String,
    private var toCity: String,
    private val onTimeSelected: (RouteTime) -> Unit
) : ListAdapter<RouteTime, RouteTimeAdapter.RouteTimeViewHolder>(DiffCallback) {

    fun updateRouteCities(from: String, to: String) {
        this.fromCity = from
        this.toCity = to
        notifyItemRangeChanged(0, itemCount)
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

    inner class RouteTimeViewHolder(
        private val binding: ItemRouteTimeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RouteTime) {
            Log.e("Route",item.toString())

            binding.tvDepartureTime.text = AppUtils.formatToAmPm(item.routeTime.orEmpty())
            binding.tvType.text = item.timeType ?: "Regular"

            // Bound from the cities passed from the Activity
            binding.tvFromCity.text = fromCity
            binding.tvToCity.text = toCity

            binding.btnConfirm.setOnClickListener {
                onTimeSelected(item)
            }
            binding.root.setOnClickListener {
                onTimeSelected(item)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<RouteTime>() {
        override fun areItemsTheSame(oldItem: RouteTime, newItem: RouteTime): Boolean {
            return oldItem.routeTimeId == newItem.routeTimeId
        }

        override fun areContentsTheSame(oldItem: RouteTime, newItem: RouteTime): Boolean {
            return oldItem == newItem
        }
    }
}
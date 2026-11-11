package com.example.kohistanbookingapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kohistanbookingapp.databinding.ItemRouteTimeBinding
import com.example.kohistanbookingapp.models.response.RouteData
import com.example.kohistanbookingapp.util.AppUtils

class RouteTimeAdapter(
    private val onTimeSelected: (RouteData) -> Unit
) : ListAdapter<RouteData, RouteTimeAdapter.RouteTimeViewHolder>(DiffCallback) {

    fun updateRouteCities(from: String, to: String) {
        /*this.fromCity = from
        this.toCity = to*/
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

        fun bind(item: RouteData) {
            Log.e("Route",item.toString())

            binding.tvDepartureTime.text = AppUtils.formatToAmPm(item.routeTimeValue.orEmpty())
            binding.tvType.text = item.routeTimeType ?: "Regular"
            binding.tvPrice.text = item.fare.toString()

            // Bound from the cities passed from the Activity
            binding.tvFromCity.text = item.fromCity
            binding.tvToCity.text = item.toCity

            binding.btnConfirm.setOnClickListener {
                onTimeSelected(item)
            }
            binding.root.setOnClickListener {
                onTimeSelected(item)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<RouteData>() {
        override fun areItemsTheSame(oldItem: RouteData, newItem: RouteData): Boolean {
            return oldItem.routeTimeId == newItem.routeTimeId
        }

        override fun areContentsTheSame(oldItem: RouteData, newItem: RouteData): Boolean {
            return oldItem == newItem
        }
    }
}
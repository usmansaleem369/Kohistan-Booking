package com.example.kohistanbookingapp.adapters


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kohistanbookingapp.databinding.ItemDropdownCityCustomBinding
import com.example.kohistanbookingapp.models.response.CityData

class CitySearchAdapter(
    private val onCitySelected: (CityData) -> Unit
) : RecyclerView.Adapter<CitySearchAdapter.CityViewHolder>() {

    private var allCities: List<CityData> = emptyList()
    private var displayedCities: List<CityData> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<CityData>) {
        allCities = list
        displayedCities = list
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        val cleanQuery = query.trim().lowercase()
        displayedCities = if (cleanQuery.isEmpty()) {
            allCities
        } else {
            allCities.filter { city ->
                city.cityName?.lowercase()?.contains(cleanQuery)?:false
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val binding = ItemDropdownCityCustomBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        holder.bind(displayedCities[position])
    }

    override fun getItemCount(): Int = displayedCities.size

    inner class CityViewHolder(
        private val binding: ItemDropdownCityCustomBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(city: CityData) {
            binding.tvCityName.text = city.cityName

          /*  if (!city.terminal.isNullOrBlank()) {
                binding.tvTerminalInfo.text = city.terminal
                binding.tvTerminalInfo.visibility = View.VISIBLE
            } else {
                binding.tvTerminalInfo.visibility = View.GONE
            }
*/
            binding.root.setOnClickListener {
                val currentPosition = bindingAdapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onCitySelected(displayedCities[currentPosition])
                }
            }
        }
    }
}
package com.example.kohistanbookingapp.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kohistanbookingapp.databinding.ViewSearchableDropdownPopupBinding
import com.example.kohistanbookingapp.models.response.CityData
import androidx.core.graphics.drawable.toDrawable

class SearchableDropdownPopup(
    private val context: Context,
    private val anchorView: View,
    private val cities: List<CityData>,
    private val onCitySelected: (CityData) -> Unit
) {

    private val binding = ViewSearchableDropdownPopupBinding.inflate(LayoutInflater.from(context))
    private val popupWindow = PopupWindow(
        binding.root,
        anchorView.width.takeIf { it > 0 } ?: ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        true
    )

    private val adapter = CitySearchAdapter { selectedCity ->
        onCitySelected(selectedCity)
        popupWindow.dismiss()
    }

    init {
        popupWindow.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 16f
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE

        binding.rvPopupCities.layoutManager = LinearLayoutManager(context)
        binding.rvPopupCities.adapter = adapter
        adapter.submitList(cities)

        binding.etPopupSearch.addTextChangedListener { text ->
            adapter.filter(text?.toString().orEmpty())
        }
    }

    fun show() {
        // Ensure width matches anchor width even on first load
        popupWindow.width = anchorView.width
        binding.etPopupSearch.text?.clear()
        adapter.filter("")

        popupWindow.showAsDropDown(anchorView, 0, 8)

        // Request focus and open keyboard for the search input
        binding.etPopupSearch.post {
            binding.etPopupSearch.requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(binding.etPopupSearch, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}
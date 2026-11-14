package com.example.kohistanbookingapp.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.kohistanbookingapp.R
import com.example.kohistanbookingapp.databinding.ItemBusSeatBinding
import com.example.kohistanbookingapp.models.response.SeatCell
import com.example.kohistanbookingapp.models.response.SeatGender
import com.example.kohistanbookingapp.models.response.SeatPatternRow
import com.example.kohistanbookingapp.models.response.SeatStatus
import com.example.kohistanbookingapp.models.response.SelectedSeat

class BusSeatAdapter(
    private val maxSelection: Int = 6,
    private val onRequestGenderSelection: (seatNumber: Int) -> Unit,
    private val onSelectionChanged: (selectedSeats: List<SelectedSeat>) -> Unit,
    private val onSeatError: (message: String) -> Unit
) : RecyclerView.Adapter<BusSeatAdapter.SeatViewHolder>() {

    companion object {
        private const val COLS = 5
        private const val HOSTESS_ADJACENT_SEAT = 4
    }

    private val seatGrid = mutableListOf<MutableList<SeatCell>>()
    private val seatPositionMap = HashMap<Int, Int>()
    private val selectedSeatsMap = linkedMapOf<Int, SelectedSeat>()
    private var containerHeightPx: Int = 0

    private fun getCellAt(position: Int): SeatCell =
        seatGrid[position / COLS][position % COLS]

    private fun updateCellAt(position: Int, updated: SeatCell) {
        seatGrid[position / COLS][position % COLS] = updated
    }

    private fun findPositionBySeatNumber(seatNumber: Int): Int =
        seatPositionMap[seatNumber] ?: -1

    fun setContainerHeight(heightPx: Int) {
        if (heightPx > 0 && containerHeightPx != heightPx) {
            containerHeightPx = heightPx
            if (seatGrid.isNotEmpty()) {
                notifyItemRangeChanged(0, itemCount)
            }
        }
    }

    fun submitSeatPattern(
        rows: List<SeatPatternRow>,
        maleBookedSeats: Set<Int> = emptySet(),
        femaleBookedSeats: Set<Int> = emptySet()
    ) {
        selectedSeatsMap.clear()
        seatGrid.clear()
        seatPositionMap.clear()

        rows.forEachIndexed { rowIndex, row ->
            val columns = listOf(row.c1, row.c2, row.c3, row.c4, row.c5)
            val rowCells = columns.mapIndexed { colIndex, seatNum ->
                if (seatNum > 0) {
                    seatPositionMap[seatNum] = rowIndex * COLS + colIndex
                }

                val (status, gender) = when {
                    seatNum <= 0 -> SeatStatus.AISLE to SeatGender.NONE
                    femaleBookedSeats.contains(seatNum) -> SeatStatus.BOOKED_FEMALE to SeatGender.FEMALE
                    maleBookedSeats.contains(seatNum) -> SeatStatus.BOOKED_MALE to SeatGender.MALE
                    else -> SeatStatus.AVAILABLE to SeatGender.NONE
                }
                SeatCell(
                    seatNumber = seatNum,
                    rowIndex = rowIndex,
                    colIndex = colIndex,
                    status = status,
                    gender = gender
                )
            }.toMutableList()
            seatGrid.add(rowCells)
        }

        notifyDataSetChanged()
        onSelectionChanged(emptyList())
    }

    fun confirmSeatSelection(seatNumber: Int, chosenGender: SeatGender) {
        val pos = findPositionBySeatNumber(seatNumber)
        if (pos == -1) return
        val selectedSeat = getCellAt(pos)

        // 1. Seat 4 is strictly Female-only (adjacent to Hostess seat 3)
        if (seatNumber == HOSTESS_ADJACENT_SEAT && chosenGender != SeatGender.FEMALE) {
            onSeatError("Seat 4 is next to the hostess seat and is reserved for female passengers only.")
            return
        }

        // 2. Check adjacent pre-booked passenger gender rules
        val lockedGender = if (seatNumber == HOSTESS_ADJACENT_SEAT) {
            SeatGender.FEMALE
        } else {
            getRequiredGenderFromBookedNeighbors(selectedSeat)
        }

        if (lockedGender != null && lockedGender != chosenGender) {
            val errorMsg = if (lockedGender == SeatGender.FEMALE) {
                "Male cannot be seated next to a female passenger."
            } else {
                "Female cannot be seated next to a male passenger."
            }
            onSeatError(errorMsg)
            return
        }

        if (selectedSeatsMap.size >= maxSelection && !selectedSeatsMap.containsKey(seatNumber)) {
            onSeatError("Maximum $maxSelection seats allowed per booking.")
            return
        }

        selectedSeatsMap[seatNumber] = SelectedSeat(
            seatNumber = seatNumber,
            requiredGender = lockedGender,
            assignedGender = chosenGender
        )
        updateCellAt(pos, selectedSeat.copy(status = SeatStatus.SELECTED, gender = chosenGender))

        notifyItemChanged(pos)
        onSelectionChanged(selectedSeatsMap.values.toList())
    }

    fun getSelectedSeats(): List<SelectedSeat> = selectedSeatsMap.values.toList()

    private fun getRequiredGenderFromBookedNeighbors(selectedSeat: SeatCell): SeatGender? {
        val row = seatGrid.getOrNull(selectedSeat.rowIndex) ?: return null

        val neighbors = listOfNotNull(
            row.getOrNull(selectedSeat.colIndex - 1),
            row.getOrNull(selectedSeat.colIndex + 1)
        ).filter { it.seatNumber > 0 }

        val hasBookedFemale = neighbors.any { it.status == SeatStatus.BOOKED_FEMALE }
        val hasBookedMale = neighbors.any { it.status == SeatStatus.BOOKED_MALE }

        return when {
            hasBookedFemale && hasBookedMale -> null
            hasBookedFemale -> SeatGender.FEMALE
            hasBookedMale -> SeatGender.MALE
            else -> null
        }
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
        if (containerHeightPx > 0 && seatGrid.isNotEmpty()) {
            val rowHeight = containerHeightPx / seatGrid.size
            val params = holder.itemView.layoutParams
            if (params.height != rowHeight) {
                params.height = rowHeight
                holder.itemView.layoutParams = params
            }
        }
        holder.bind(getCellAt(position))
    }

    override fun getItemCount(): Int = seatGrid.size * COLS

    inner class SeatViewHolder(
        private val binding: ItemBusSeatBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.tvSeatNumber.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                val cell = getCellAt(pos)

                if (cell.status == SeatStatus.AISLE ||
                    cell.status == SeatStatus.BOOKED_MALE ||
                    cell.status == SeatStatus.BOOKED_FEMALE
                ) {
                    return@setOnClickListener
                }

                // Deselect if already selected
                if (selectedSeatsMap.containsKey(cell.seatNumber)) {
                    selectedSeatsMap.remove(cell.seatNumber)
                    updateCellAt(pos, cell.copy(status = SeatStatus.AVAILABLE, gender = SeatGender.NONE))
                    notifyItemChanged(pos)
                    onSelectionChanged(selectedSeatsMap.values.toList())
                    return@setOnClickListener
                }

                if (selectedSeatsMap.size >= maxSelection) {
                    onSeatError("Maximum $maxSelection seats allowed per booking.")
                    return@setOnClickListener
                }

                onRequestGenderSelection(cell.seatNumber)
            }
        }

        fun bind(cell: SeatCell) {
            val context = binding.root.context
            when (cell.status) {
                SeatStatus.AISLE -> {
                    binding.tvSeatNumber.visibility = View.INVISIBLE
                    binding.tvSeatNumber.isEnabled = false
                }

                SeatStatus.BOOKED_MALE -> {
                    binding.tvSeatNumber.visibility = View.VISIBLE
                    binding.tvSeatNumber.isEnabled = false
                    binding.tvSeatNumber.text = cell.seatNumber.toString()
                    binding.tvSeatNumber.setBackgroundResource(R.drawable.bg_seat_booked_male)
                    binding.tvSeatNumber.setTextColor("#1D39C4".toColorInt())
                }

                SeatStatus.BOOKED_FEMALE -> {
                    binding.tvSeatNumber.visibility = View.VISIBLE
                    binding.tvSeatNumber.isEnabled = false
                    binding.tvSeatNumber.text = cell.seatNumber.toString()
                    binding.tvSeatNumber.setBackgroundResource(R.drawable.bg_seat_booked_female)
                    binding.tvSeatNumber.setTextColor("#C41D7F".toColorInt())
                }

                SeatStatus.AVAILABLE -> {
                    binding.tvSeatNumber.visibility = View.VISIBLE
                    binding.tvSeatNumber.isEnabled = true
                    binding.tvSeatNumber.text = cell.seatNumber.toString()
                    binding.tvSeatNumber.setBackgroundResource(R.drawable.bg_seat_available)
                    binding.tvSeatNumber.setTextColor(
                        ContextCompat.getColor(context, R.color.btn_navy)
                    )
                }

                SeatStatus.SELECTED -> {
                    binding.tvSeatNumber.visibility = View.VISIBLE
                    binding.tvSeatNumber.isEnabled = true
                    binding.tvSeatNumber.text = cell.seatNumber.toString()
                    val bgRes = if (cell.gender == SeatGender.FEMALE) {
                        R.drawable.bg_seat_selected_female
                    } else {
                        R.drawable.bg_seat_selected
                    }
                    binding.tvSeatNumber.setBackgroundResource(bgRes)
                    binding.tvSeatNumber.setTextColor(Color.WHITE)
                }
            }
        }
    }
}
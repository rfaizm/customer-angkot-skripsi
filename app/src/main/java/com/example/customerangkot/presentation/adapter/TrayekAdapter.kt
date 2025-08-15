package com.example.customerangkot.presentation.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.customerangkot.R
import com.example.customerangkot.data.api.dto.DataTrayekJSON
import com.example.customerangkot.domain.entity.TrayekItem
import com.example.customerangkot.utils.AppColors

class TrayekAdapter(
    private val trayekList: List<TrayekItem>,
    private val onSelectionChanged: (TrayekItem?) -> Unit
) : RecyclerView.Adapter<TrayekAdapter.TrayekViewHolder>() {

    private var filteredTrayekList: MutableList<TrayekItem> = trayekList.toMutableList() // Daftar yang ditampilkan (filtered)
    private var selectedPosition: Int = -1

    inner class TrayekViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val nameText: TextView = view.findViewById(R.id.name_trayek_angkot)

        fun bind(trayek: TrayekItem, isSelected: Boolean) {
            nameText.text = trayek.name

            if (isSelected) {
                nameText.setBackgroundResource(R.drawable.radio_selected)
                nameText.setTextColor(AppColors.Primary)
            } else {
                nameText.setBackgroundResource(R.drawable.rounded_corner_trayek_angkot)
                nameText.setTextColor(android.graphics.Color.WHITE)
            }

            nameText.setOnClickListener {
                val previousPosition = selectedPosition
                val clickedPosition = adapterPosition
                if (clickedPosition == selectedPosition) {
                    // Deselect jika item yang sama diklik
                    selectedPosition = -1
                    onSelectionChanged(null)
                } else {
                    // Pindahkan item yang dipilih ke posisi awal
                    val selectedItem = filteredTrayekList.removeAt(clickedPosition)
                    filteredTrayekList.add(0, selectedItem)
                    selectedPosition = 0 // Item sekarang di posisi awal
                    onSelectionChanged(selectedItem)
                }
                notifyItemChanged(previousPosition)
                notifyItemMoved(clickedPosition, 0)
                notifyItemChanged(0)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrayekViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trayek_angkot, parent, false)
        return TrayekViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrayekViewHolder, position: Int) {
        holder.bind(filteredTrayekList[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = filteredTrayekList.size

    fun filter(query: String?) {
        filteredTrayekList = (if (query.isNullOrBlank()) {
            trayekList
        } else {
            trayekList.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }).toMutableList()
        selectedPosition = -1 // Reset pilihan saat filter berubah
        notifyDataSetChanged()
    }

    fun setSelectedTrayekById(trayekId: Int) {
        val index = filteredTrayekList.indexOfFirst { it.trayekId == trayekId }
        if (index != -1) {
            // Pindahkan item ke posisi awal
            val selectedItem = filteredTrayekList.removeAt(index)
            filteredTrayekList.add(0, selectedItem)
            selectedPosition = 0
            notifyItemMoved(index, 0)
            notifyItemChanged(0)
            onSelectionChanged(selectedItem)
        }
    }
}

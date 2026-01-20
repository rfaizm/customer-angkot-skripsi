package com.example.customerangkot.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.customerangkot.data.api.dto.DataAngkotFilter
import com.example.customerangkot.data.api.dto.DataTrayekJSON
import com.example.customerangkot.databinding.ItemChooseAngkotBinding
import com.example.customerangkot.databinding.ItemDetailPosisiBinding

class ChooseAngkotAdapter(
    private val listAngkot: List<DataAngkotFilter>,
    private val onClick: (DataAngkotFilter) -> Unit
) : RecyclerView.Adapter<ChooseAngkotAdapter.ChooseAngkotViewHolder>() {

    inner class ChooseAngkotViewHolder(
        private val binding: ItemChooseAngkotBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(angkot: DataAngkotFilter) {
            binding.itemTrayekAngkot.text = angkot.platNomor
            binding.textDistanceKm.text =
                String.format("%.1f Km", angkot.distanceKm as? Double ?: 0.0)

            binding.sumPassengerText.text =
                angkot.currentPassengers?.toString() ?: "0"


            itemView.setOnClickListener {
                onClick(angkot)
            }
        }
    }

    fun updateDistance(
        angkotId: Int,
        newDistanceKm: Double
    ) {
        val index = listAngkot.indexOfFirst { it.angkotId == angkotId }
        if (index != -1) {
            listAngkot[index].distanceKm = newDistanceKm
            notifyItemChanged(index)
        }
    }



    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChooseAngkotViewHolder {
        val binding = ItemChooseAngkotBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ChooseAngkotViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ChooseAngkotViewHolder,
        position: Int
    ) {
        holder.bind(listAngkot[position])
    }

    override fun getItemCount() = listAngkot.size


}
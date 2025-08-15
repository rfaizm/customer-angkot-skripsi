package com.example.customerangkot.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.customerangkot.R
import com.example.customerangkot.databinding.ItemDetailPosisiBinding
import com.example.customerangkot.domain.entity.InformationItem
import com.example.customerangkot.domain.entity.TrayekItem
import com.example.customerangkot.utils.InformationTrayek

class InformationAdapter(
    private val informationList: List<InformationItem>,
    private val onClick: (InformationItem) -> Unit
) : RecyclerView.Adapter<InformationAdapter.InformationViewHolder>() {

    inner class InformationViewHolder(
        private val binding: ItemDetailPosisiBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: InformationItem) {
            when (item) {
                is InformationItem.TrayekInformation -> {
                    binding.itemTrayekAngkot.text = item.name
                    binding.textInformationTrayek.text = item.description ?: "Tidak ada deskripsi"
                    Glide.with(itemView.context)
                        .load(item.imageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .into(binding.imageAngkot)
                }
                is InformationItem.AngkotInformation -> {
                    binding.itemTrayekAngkot.text = item.platNomor
                    binding.textInformationTrayek.text = "${item.distanceKm} km"
                    Glide.with(itemView.context)
                        .load(item.imageUrl) // Baris 33: Gunakan imageUrl dari AngkotInformation
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .into(binding.imageAngkot)
                }
            }

            itemView.setOnClickListener {
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InformationViewHolder {
        val binding = ItemDetailPosisiBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return InformationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InformationViewHolder, position: Int) {
        holder.bind(informationList[position])
    }

    override fun getItemCount() = informationList.size
}
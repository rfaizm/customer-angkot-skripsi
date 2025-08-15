package com.example.customerangkot.presentation.adapter

import android.icu.text.NumberFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.customerangkot.databinding.ItemTrackBinding
import com.example.customerangkot.utils.RouteAngkot
import java.util.Locale

class RouteAdapter(
    private val routeList: List<RouteAngkot>,
    private val onRouteSelected: (RouteAngkot, startLat: Double, startLong: Double, destinationLat: Double, destinationLong: Double) -> Unit,
    private val startLat: Double,
    private val startLong: Double,
    private val destinationLat: Double,
    private val destinationLong: Double
) : RecyclerView.Adapter<RouteAdapter.RouteViewHolder>() {

    inner class RouteViewHolder(private val binding: ItemTrackBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(routeAngkot: RouteAngkot) {
            binding.trayekAngkot.text = routeAngkot.namaTrayek
            binding.textEta.text = routeAngkot.predictETA
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            binding.priceTrayek.text = formatter.format(routeAngkot.price).replace("Rp", "Rp. ").replace(",00", "")

            itemView.setOnClickListener {
                onRouteSelected(routeAngkot, startLat, startLong, destinationLat, destinationLong)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val binding = ItemTrackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RouteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.bind(routeList[position])
    }

    override fun getItemCount(): Int = routeList.size
}
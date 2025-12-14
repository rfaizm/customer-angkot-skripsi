package com.example.customerangkot.presentation.adapter

import android.icu.text.NumberFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.customerangkot.databinding.ItemTrackBinding
import com.example.customerangkot.utils.RouteAngkot
import java.util.Locale
import androidx.core.graphics.toColorInt

class RouteAdapter(
    private val routeList: List<RouteAngkot>,
    private val onRouteSelected: (RouteAngkot) -> Unit,
) : RecyclerView.Adapter<RouteAdapter.RouteViewHolder>() {

    inner class RouteViewHolder(
        private val binding: ItemTrackBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            routeAngkot: RouteAngkot,
            position: Int,
            itemCount: Int
        ) {
            binding.trayekAngkot.text = routeAngkot.namaTrayek
            binding.textEta.text = routeAngkot.predictETA
            binding.imageAngkotColor.setColorFilter(routeAngkot.color.toColorInt())


            if (routeAngkot.isIntegrated) {
                val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                binding.priceTrayek.text =
                    formatter.format(routeAngkot.price)
                        .replace("Rp", "Rp. ")
                        .replace(",00", "")
                binding.priceTrayek.visibility = View.VISIBLE
                binding.perPerson.visibility = View.VISIBLE
            } else {
                binding.priceTrayek.visibility = View.GONE
                binding.perPerson.visibility = View.GONE
            }

            // =====================================================
            // LOGIC KHUSUS JIKA HANYA 1 ITEM (TANPA TRANSIT)
            // =====================================================
            if (itemCount == 1) {

                // Sembunyikan semua garis putus-putus
                binding.dashedLineTop.visibility = View.INVISIBLE
                binding.dashedLineButtom.visibility = View.INVISIBLE

                // Tampilkan icon arrow & text "Tanpa transit"
                binding.imageWithoutTransit.visibility = View.VISIBLE
                binding.textWithoutTransit.visibility = View.VISIBLE

                // Sembunyikan icon lingkaran transit
                binding.indicateIcon.visibility = View.GONE

            } else {

                // =====================================================
                // LOGIC DEFAULT (ADA TRANSIT)
                // =====================================================

                // Sembunyikan arrow & text tanpa transit
                binding.imageWithoutTransit.visibility = View.GONE
                binding.textWithoutTransit.visibility = View.GONE

                // Tampilkan icon lingkaran transit
                binding.indicateIcon.visibility = View.VISIBLE

                // Item PERTAMA → sembunyikan garis atas
                binding.dashedLineTop.visibility =
                    if (position == 0) View.INVISIBLE else View.VISIBLE

                // Item TERAKHIR → sembunyikan garis bawah
                binding.dashedLineButtom.visibility =
                    if (position == itemCount - 1) View.INVISIBLE else View.VISIBLE
            }

            // =====================================================

            itemView.setOnClickListener {
                onRouteSelected(routeAngkot)
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
        holder.bind(
            routeAngkot = routeList[position],
            position = position,
            itemCount = itemCount
        )
    }

    override fun getItemCount(): Int = routeList.size
}


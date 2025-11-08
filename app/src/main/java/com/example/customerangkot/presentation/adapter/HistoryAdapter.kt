package com.example.customerangkot.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.customerangkot.data.api.dto.DataHistoryItem
import com.example.customerangkot.databinding.ItemHistoryBinding
import com.example.customerangkot.utils.DataHistory
import com.example.customerangkot.utils.Utils.capitalizeWords
import java.util.Locale

class HistoryAdapter(
    private val dataHistory: List<DataHistoryItem>
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(dataHistory: DataHistoryItem) {
            binding.trayekAngkot.text = dataHistory.trayek?.capitalizeWords() ?: "-"
            binding.fullname.text = dataHistory.driverName?.capitalizeWords() ?: "-"
            binding.numberPlat.text = dataHistory.vehiclePlate?.capitalizeWords() ?: "-"
            binding.date.text = dataHistory.orderDate?.capitalizeWords() ?: "-"
            binding.methodPayment.text = dataHistory.paymentMethod?.capitalizeWords() ?: "-"
            // Format harga
            binding.price.text = dataHistory.totalPrice?.let {
                "Rp. ${String.format(Locale("id", "ID"), "%,d", it).replace(",", ".")}"
            } ?: "-"
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        dataHistory[position].let { holder.bind(it) }
    }

    override fun getItemCount() = dataHistory.size
}
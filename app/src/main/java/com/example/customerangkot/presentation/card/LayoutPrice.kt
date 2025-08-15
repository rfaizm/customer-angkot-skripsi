package com.example.customerangkot.presentation.card

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.customerangkot.databinding.LayoutPriceBinding

class LayoutPrice @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding = LayoutPriceBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        // Any setup you want
    }

    fun setPrice(price: String) {
        binding.rupiahPrice.text = price
    }
}

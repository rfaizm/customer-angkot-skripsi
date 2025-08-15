package com.example.customerangkot.presentation.card

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import com.example.customerangkot.R
import com.example.customerangkot.databinding.LayoutPositionAngkotBinding


class LayoutPositionAngkot @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CardView(context, attrs) {

    private val binding: LayoutPositionAngkotBinding =
        LayoutPositionAngkotBinding.inflate(LayoutInflater.from(context), this, true)

    // Expose the FrameLayout to the parent
    val frameMaps: FrameLayout = binding.frameMaps

    init {
        setCardBackgroundColor(resources.getColor(R.color.white, null))
    }
}
package com.example.customerangkot.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalSpaceItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position != RecyclerView.NO_POSITION) {
            val adapter = parent.adapter
            if (adapter != null && position < adapter.itemCount - 1) {
                outRect.right = spacing // Tambahkan jarak di sebelah kanan setiap item kecuali yang terakhir
            }
        }
    }
}
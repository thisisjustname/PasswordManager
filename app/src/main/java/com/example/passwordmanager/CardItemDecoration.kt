package com.example.passwordmanager

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class CardItemDecoration(private val margin: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        with(outRect) {
            left = margin
            right = margin
            top = margin
            if (parent.getChildAdapterPosition(view) == 0) {
                top = margin
            }
        }
    }
}
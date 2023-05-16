package ru.gamebreaker.tabladeanuncioskotlin.presentation.adapters

import android.app.AlertDialog
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.gamebreaker.tabladeanuncioskotlin.R
import ru.gamebreaker.tabladeanuncioskotlin.databinding.SpListItemBinding

class CategoryViewHolder (binding: SpListItemBinding, var tvSelection: TextView, var dialog: AlertDialog) :
    RecyclerView.ViewHolder(binding.root), View.OnClickListener {
    private var itemText = ""

    fun setData(text: String) {
        val tvSpItem = itemView.findViewById<TextView>(R.id.tvSpItem)
        tvSpItem.text = text
        itemText = text
        itemView.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        tvSelection.text = itemText
        dialog.dismiss()
    }
}
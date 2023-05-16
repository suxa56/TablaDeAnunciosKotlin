package ru.gamebreaker.tabladeanuncioskotlin.presentation.adapters

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.gamebreaker.tabladeanuncioskotlin.databinding.SpListItemBinding


class CategoryRVAdapter(var tvSelection: TextView, var dialog: AlertDialog) :
    RecyclerView.Adapter<CategoryViewHolder>() {
    private val mainList = ArrayList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = SpListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding, tvSelection, dialog)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.setData(mainList[position])
    }

    override fun getItemCount(): Int {
        return mainList.size
    }

    fun updateAdapter(list: ArrayList<String>) {
        mainList.clear()
        mainList.addAll(list)
        notifyDataSetChanged()
    }
}
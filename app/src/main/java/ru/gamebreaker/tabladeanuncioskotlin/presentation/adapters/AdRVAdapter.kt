package ru.gamebreaker.tabladeanuncioskotlin.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import ru.gamebreaker.tabladeanuncioskotlin.databinding.AdListItemBinding
import ru.gamebreaker.tabladeanuncioskotlin.domain.model.Ad
import ru.gamebreaker.tabladeanuncioskotlin.presentation.MainActivity
import java.text.SimpleDateFormat
import java.util.*

class AdRVAdapter(val act: MainActivity) : ListAdapter<Ad, AdViewHolder>(AdDiffUtil) {
    private var timeFormatter: SimpleDateFormat? = null

    init {
        timeFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdViewHolder {
        val binding = AdListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdViewHolder(binding, act, timeFormatter!!)
    }

    override fun onBindViewHolder(holder: AdViewHolder, position: Int) {
        holder.setData(getItem(position))
    }

    interface Listener {
        fun onDeleteItem(ad: Ad)
        fun onAdViewed(ad: Ad)
        fun onFavClicked(ad: Ad)
    }

}
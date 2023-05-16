package ru.gamebreaker.tabladeanuncioskotlin.presentation.adapters

import androidx.recyclerview.widget.DiffUtil
import ru.gamebreaker.tabladeanuncioskotlin.domain.model.Ad

object AdDiffUtil: DiffUtil.ItemCallback<Ad>() {

    override fun areItemsTheSame(oldItem: Ad, newItem: Ad): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: Ad, newItem: Ad): Boolean {
        return oldItem == newItem
    }
}
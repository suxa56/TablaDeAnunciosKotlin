package ru.gamebreaker.tabladeanuncioskotlin.presentation.adapters

import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ru.gamebreaker.tabladeanuncioskotlin.R
import ru.gamebreaker.tabladeanuncioskotlin.databinding.AdListItemBinding
import ru.gamebreaker.tabladeanuncioskotlin.domain.model.Ad
import ru.gamebreaker.tabladeanuncioskotlin.presentation.MainActivity
import ru.gamebreaker.tabladeanuncioskotlin.presentation.EditAdsAct
import java.text.SimpleDateFormat
import java.util.*

class AdViewHolder(
    val binding: AdListItemBinding,
    val act: MainActivity,
    val formatter: SimpleDateFormat
) :
    RecyclerView.ViewHolder(binding.root) {

    private val isAnonymous = act.mAuth.currentUser?.isAnonymous

    fun setData(ad: Ad) = with(binding) {
        tvDescription.text = ad.description
        tvPriceName.text = ad.price
        tvTitle.text = ad.title
        tvViewCounter.text = ad.viewsCounter
        if (isAnonymous!!) {
            ibFav.visibility = View.GONE
            tvFavCounter.visibility = View.GONE
        } else {
            tvFavCounter.text = ad.favCounter
        }

        val publishTime = "Время публикации: ${getTimeFromMillis(ad.time)}"
        tvPublishTime.text = publishTime

        Picasso.get().load(ad.mainImage).into(mainImage)

        isFav(ad)
        showEditPanel(isOwner(ad))
        mainOnClick(ad)
    }

    private fun getTimeFromMillis(timeMillis: String): String {
        val c = Calendar.getInstance()
        c.timeInMillis = timeMillis.toLong()
        return formatter.format(c.time)
    }

    private fun mainOnClick(ad: Ad) = with(binding) {
        ibFav.setOnClickListener {
            if (act.mAuth.currentUser?.isAnonymous == false) act.onFavClicked(ad)
        }
        itemView.setOnClickListener {
            act.onAdViewed(ad)
        }
        ibEditAd.setOnClickListener(onClickEdit(ad))
        ibDelAd.setOnClickListener {
            act.onDeleteItem(ad)
        }
    }

    private fun isFav(ad: Ad) {
        if (ad.isFav) {
            binding.ibFav.setImageResource(R.drawable.ic_favorite_on)
        } else {
            binding.ibFav.setImageResource(R.drawable.ic_favorite_off)
        }
    }

    private fun onClickEdit(ad: Ad): View.OnClickListener {
        return View.OnClickListener {
            val editIntent = Intent(act, EditAdsAct::class.java).apply {
                putExtra(MainActivity.EDIT_STATE, true)
                putExtra(MainActivity.ADS_DATA, ad)
            }
            act.startActivity(editIntent)
        }
    }

    private fun isOwner(ad: Ad): Boolean {
        return ad.uid == act.mAuth.uid
    }

    private fun showEditPanel(isOwner: Boolean) {
        if (isOwner) {
            binding.editPanel.visibility = View.VISIBLE
        } else {
            binding.editPanel.visibility = View.GONE
        }
    }
}
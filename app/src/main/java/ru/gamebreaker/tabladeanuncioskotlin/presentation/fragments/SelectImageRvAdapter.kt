package ru.gamebreaker.tabladeanuncioskotlin.presentation.fragments

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.gamebreaker.tabladeanuncioskotlin.presentation.EditAdsAct
import ru.gamebreaker.tabladeanuncioskotlin.databinding.SelectImageFragmentItemBinding
import ru.gamebreaker.tabladeanuncioskotlin.utils.AdapterCallBack
import ru.gamebreaker.tabladeanuncioskotlin.utils.ImageManager
import ru.gamebreaker.tabladeanuncioskotlin.utils.ImagePicker
import ru.gamebreaker.tabladeanuncioskotlin.utils.ItemTouchMoveCallBack

class SelectImageRvAdapter(val adapterCallBack: AdapterCallBack) :
    RecyclerView.Adapter<SelectImageRvAdapter.ImageHolder>(),
    ItemTouchMoveCallBack.ItemTouchAdapter {
    val mainArray = ArrayList<Bitmap>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val viewBinding = SelectImageFragmentItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageHolder(viewBinding, parent.context, this)
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.setData(mainArray[position])
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }

    override fun onMove(startPos: Int, targetPos: Int) {
        val targetItem = mainArray[targetPos]
        mainArray[targetPos] = mainArray[startPos]
        //val titleStart = mainArray[targetPos].title
        //mainArray[targetPos].title = targetItem.title
        mainArray[startPos] = targetItem
        //mainArray[startPos].title = titleStart
        notifyItemMoved(startPos, targetPos)
    }

    override fun onClear() {
        notifyDataSetChanged()
    }

    class ImageHolder(
        private val viewBinding: SelectImageFragmentItemBinding, val context: Context,
        val adapter: SelectImageRvAdapter
    ) : RecyclerView.ViewHolder(viewBinding.root) {

        fun setData(bitmap: Bitmap) {

            viewBinding.imEditImage.setOnClickListener {
                ImagePicker.getSingleImage(context as EditAdsAct)
                context.editImagePos = adapterPosition
            }

            viewBinding.imDelete.setOnClickListener {

                adapter.mainArray.removeAt(adapterPosition)
                adapter.notifyItemRemoved(adapterPosition)
                for (n in 0 until adapter.mainArray.size) adapter.notifyItemChanged(n)
                adapter.adapterCallBack.onItemDelete()

            }

            ImageManager.chooseScaleType(viewBinding.imageContent, bitmap)
            viewBinding.imageContent.setImageBitmap(bitmap)

        }
    }

    fun updateAdapter(newList: List<Bitmap>, needClear: Boolean) {
        if (needClear) mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }

}
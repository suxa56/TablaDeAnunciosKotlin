package ru.gamebreaker.tabladeanuncioskotlin.utils

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import io.ak1.pix.models.Ratio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.gamebreaker.tabladeanuncioskotlin.R
import ru.gamebreaker.tabladeanuncioskotlin.presentation.EditAdsAct

object ImagePicker {

    const val MAX_IMAGE_COUNT = 3

    private fun getOptions(imageCounter: Int): Options {
        val options = Options().apply {
            count = imageCounter
            ratio = Ratio.RATIO_AUTO
            isFrontFacing = false
            mode = Mode.Picture
            path = "/pix/images"
        }
        return options
    }

    fun getMultiImages(edAct: EditAdsAct, imageCounter: Int) {
        edAct.addPixToActivity(R.id.place_holder, getOptions(imageCounter)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    getMultiSelectImages(edAct, result.data)
                    closePixFrag(edAct)
                }
                else -> {}
            }
        }
    }

    fun addImages(edAct: EditAdsAct, imageCounter: Int) {
        edAct.addPixToActivity(R.id.place_holder, getOptions(imageCounter)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    openChooseImageFragment(edAct)
                    edAct.chooseImageFragment?.updateAdapter(result.data as ArrayList<Uri>, edAct)
                }
                else -> {}
            }
        }
    }

    fun getSingleImage(edAct: EditAdsAct) {
        edAct.addPixToActivity(R.id.place_holder, getOptions(1)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    openChooseImageFragment(edAct)
                    singleImage(edAct, result.data[0])
                }
                else -> {}
            }
        }
    }

    private fun openChooseImageFragment(edAct: EditAdsAct) {
        edAct.supportFragmentManager.beginTransaction()
            .replace(R.id.place_holder, edAct.chooseImageFragment!!).commit()

    }

    private fun closePixFrag(edAct: EditAdsAct) {
        val fList = edAct.supportFragmentManager.fragments
        fList.forEach {
            if (it.isVisible) edAct.supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    private fun getMultiSelectImages(edAct: EditAdsAct, uris: List<Uri>) {
        if (uris.size > 1 && edAct.chooseImageFragment == null) {
            edAct.openChooseItemFragment(uris as ArrayList<Uri>)

        } else if (uris.size == 1 && edAct.chooseImageFragment == null) {

            CoroutineScope(Dispatchers.Main).launch {
                edAct.binding.pBarLoading.visibility = View.VISIBLE
                val bitmapArray =
                    ImageManager.imageResize(uris as ArrayList<Uri>, edAct) as ArrayList<Bitmap>
                edAct.binding.pBarLoading.visibility = View.GONE
                edAct.imageAdapter.update(bitmapArray)
                closePixFrag(edAct)
            }
        }
    }


    private fun singleImage(edAct: EditAdsAct, uri: Uri) {
        edAct.chooseImageFragment?.setSingleImage(uri, edAct.editImagePos)
    }
}
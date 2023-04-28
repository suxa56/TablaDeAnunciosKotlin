package ru.gamebreaker.tabladeanuncioskotlin.act

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import ru.gamebreaker.tabladeanuncioskotlin.MainActivity
import ru.gamebreaker.tabladeanuncioskotlin.R
import ru.gamebreaker.tabladeanuncioskotlin.adapters.ImageAdapter
import ru.gamebreaker.tabladeanuncioskotlin.databinding.ActivityEditAdsBinding
import ru.gamebreaker.tabladeanuncioskotlin.dialogs.DialogSpinnerHelper
import ru.gamebreaker.tabladeanuncioskotlin.fragments.FragmentCloseInterface
import ru.gamebreaker.tabladeanuncioskotlin.fragments.ImageListFragment
import ru.gamebreaker.tabladeanuncioskotlin.model.Ad
import ru.gamebreaker.tabladeanuncioskotlin.model.DbManager
import ru.gamebreaker.tabladeanuncioskotlin.utils.ImageManager
import ru.gamebreaker.tabladeanuncioskotlin.utils.ImagePicker
import java.io.ByteArrayOutputStream


class EditAdsAct : AppCompatActivity(), FragmentCloseInterface {

    var chooseImageFragment: ImageListFragment? = null
    lateinit var binding: ActivityEditAdsBinding
    private val dialog = DialogSpinnerHelper()
    lateinit var imageAdapter: ImageAdapter
    private val dbManager = DbManager()
    var editImagePos = 0
    private var imageIndex = 0
    private var isEditState = false
    private var ad: Ad? = null
    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAdsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        init()
        checkEditState()
        imageChangeCounter()
        configureToolbar()
    }

    private fun configureToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar?.navigationIcon?.setTint(getColor(R.color.onPrimaryContainer))
        if (isEditState) {
            supportActionBar?.title = ad?.title
        } else {
            supportActionBar?.title = getString(R.string.new_ad)
        }
        toolbar?.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkEditState() {
        isEditState = isEditState()
        if (isEditState) {
            ad = intent.getSerializableExtra(MainActivity.ADS_DATA) as Ad
            if (ad != null) fillViews(ad!!)
        }
    }

    private fun isEditState(): Boolean {
        return intent.getBooleanExtra(MainActivity.EDIT_STATE, false)
    }

    private fun fillViews(ad: Ad) = with(binding) {
        etTelValue.setText(ad.tel)
        spCategoryValue.text = changeCategory(ad.category)
        etTitleValue.setText(ad.title)
        etPriceValue.setText(ad.price)
        etDescriptionValue.setText(ad.description)
        updateImageCounter(0)
        ImageManager.fillImageArray(ad, imageAdapter)
    }

    private fun init() {
        toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        imageAdapter = ImageAdapter()
        binding.vpImages.adapter = imageAdapter
    }

    //OnClicks
    fun onClickSelectCategory(view: View) {
        val listCategory = resources.getStringArray(R.array.category).toMutableList() as ArrayList
        dialog.showSpinnerDialog(this, listCategory, binding.spCategoryValue)
    }

    fun onClickGetImages(view: View) {
        if (imageAdapter.mainArray.size == 0) {
            ImagePicker.getMultiImages(this, 3)
        } else {
            openChooseItemFragment(null)
            chooseImageFragment?.updateAdapterFromEdit(imageAdapter.mainArray)
        }
    }

    fun onClickPublish(view: View) {
        if (isFieldsEmpty()) {
            showToast("Внимание! Все поля * должны быть заполнены!")
            return
        }
        binding.progressLayout.visibility = View.VISIBLE
        ad = fillAd()
        uploadImages()
    }

    private fun isFieldsEmpty(): Boolean = with(binding) {
        return spCategoryValue.text.toString() == getString(R.string.select_category)
                || etTelValue.text.isEmpty()
                || etTitleValue.text.isEmpty()
                || etPriceValue.text.isEmpty()
                || etDescriptionValue.text.isEmpty()
                || imageAdapter.mainArray.size == 0
    }

    private fun onPublishFinish(): DbManager.FinishWorkListener {
        return object : DbManager.FinishWorkListener {
            override fun onFinish(isDome: Boolean) {
                binding.progressLayout.visibility = View.GONE
                if (isDome) finish()
            }
        }
    }

    private fun fillAd(): Ad {
        val adTemp: Ad
        binding.apply {
            adTemp = Ad(
                "Дворфы",
                "Тормин",
                etTelValue.text.toString(),
                Firebase.auth.currentUser!!.email,
                "1",
                "false",
                setCategory(),
                etPriceValue.text.toString(),
                etTitleValue.text.toString(),
                etDescriptionValue.text.toString(),
                ad?.mainImage ?: "empty",
                ad?.secondImage ?: "empty",
                ad?.thirdImage ?: "empty",
                ad?.key ?: dbManager.db.push().key,
                "0",
                dbManager.auth.uid,
                ad?.time ?: System.currentTimeMillis().toString()
            )
        }
        return adTemp
    }

    private fun setCategory(): String {
        val dbCategory = when (binding.spCategoryValue.text.toString()) {
            getString(R.string.ad_auto) -> R.string.ad_heroes
            getString(R.string.ad_device) -> R.string.ad_faction_war
            getString(R.string.ad_child) -> R.string.ad_arena
            getString(R.string.ad_house) -> R.string.ad_dungeons
            getString(R.string.ad_service) -> R.string.ad_cb
            getString(R.string.ad_work) -> R.string.ad_tower
            getString(R.string.ad_pet) -> R.string.lf_clan
            getString(R.string.ad_sport) -> R.string.lf_members
            else -> {
                R.string.ad_heroes
            }
        }
        return getString(dbCategory)
    }

    private fun changeCategory(categoryFromDb: String?): String {
        val shownCategory = when (categoryFromDb) {
            getString(R.string.ad_heroes) -> R.string.ad_auto
            getString(R.string.ad_faction_war) -> R.string.ad_device
            getString(R.string.ad_arena) -> R.string.ad_child
            getString(R.string.ad_dungeons) -> R.string.ad_house
            getString(R.string.ad_cb) -> R.string.ad_service
            getString(R.string.ad_tower) -> R.string.ad_work
            getString(R.string.lf_clan) -> R.string.ad_pet
            getString(R.string.lf_members) -> R.string.ad_sport
            else -> {
                R.string.ad_auto
            }
        }
        return getString(shownCategory)
    }

    override fun onFragmentClose(list: ArrayList<Bitmap>) {
        binding.scrollViewMain.visibility = View.VISIBLE
        imageAdapter.update(list)
        chooseImageFragment = null
        updateImageCounter(binding.vpImages.currentItem)
    }

    fun openChooseItemFragment(newList: ArrayList<Uri>?) {
        chooseImageFragment = ImageListFragment()
        if (newList != null) chooseImageFragment?.resizeSelectedImages(newList, true, this)
        binding.scrollViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.place_holder, chooseImageFragment!!)
        fm.commit()
    }

    private fun uploadImages() {
        if (imageIndex == 3) {
            dbManager.publishAd(ad!!, onPublishFinish())
            return
        }
        val oldUrl = getUelFromAd()
        if (imageAdapter.mainArray.size > imageIndex) {
            val byteArray = prepareImageByteArray(imageAdapter.mainArray[imageIndex])
            if (oldUrl.startsWith("http")) {
                updateImage(byteArray, oldUrl) {
                    nextImage(it.result.toString())
                }
            } else {
                uploadImage(byteArray) {
                    //dbManager.publishAd(ad!!, onPublishFinish())
                    nextImage(it.result.toString())
                }
            }
        } else {
            if (oldUrl.startsWith("http")) {
                deleteImageByUrl(oldUrl) {
                    nextImage("empty")
                }
            } else {
                nextImage("empty")
            }
        }
    }

    private fun nextImage(uri: String) {
        setImageUriToAd(uri)
        imageIndex++
        uploadImages()
    }

    private fun setImageUriToAd(uri: String) {
        when (imageIndex) {
            0 -> ad = ad?.copy(mainImage = uri)
            1 -> ad = ad?.copy(secondImage = uri)
            2 -> ad = ad?.copy(thirdImage = uri)
        }
    }

    private fun getUelFromAd(): String {
        return listOf(ad?.mainImage!!, ad?.secondImage!!, ad?.thirdImage!!)[imageIndex]
    }

    private fun prepareImageByteArray(bitmap: Bitmap): ByteArray {
        val outStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outStream)
        return outStream.toByteArray()
    }

    private fun uploadImage(byteArray: ByteArray, listener: OnCompleteListener<Uri>) {
        val imStorageRef = dbManager.dbStorage
            .child(dbManager.auth.uid!!)
            .child("image_${System.currentTimeMillis()}")
        val upTask = imStorageRef.putBytes(byteArray)
        upTask.continueWithTask {
            imStorageRef.downloadUrl
        }.addOnCompleteListener(listener)
    }

    private fun deleteImageByUrl(oldUrl: String, listener: OnCompleteListener<Void>) {
        dbManager.dbStorage.storage.getReferenceFromUrl(oldUrl).delete()
            .addOnCompleteListener(listener)
    }

    private fun updateImage(byteArray: ByteArray, url: String, listener: OnCompleteListener<Uri>) {
        val imStorageRef = dbManager.dbStorage.storage.getReferenceFromUrl(url)
        val upTask = imStorageRef.putBytes(byteArray)
        upTask.continueWithTask {
            imStorageRef.downloadUrl
        }.addOnCompleteListener(listener)
    }

    private fun imageChangeCounter() {
        binding.vpImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateImageCounter(position)
            }
        })
    }

    private fun updateImageCounter(counter: Int) {
        val index = 1
        val itemCount = binding.vpImages.adapter?.itemCount
        if (itemCount == 0) index == 0
        val imageCounter = "${counter + index}/$itemCount"
        binding.tvImageCounter.text = imageCounter
    }
}
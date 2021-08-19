package ru.gamebreaker.tabladeanuncioskotlin.act

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import ru.gamebreaker.tabladeanuncioskotlin.R
import ru.gamebreaker.tabladeanuncioskotlin.databinding.ActivityEditAdsBinding
import ru.gamebreaker.tabladeanuncioskotlin.dialogs.DialogSpinnerHelper
import ru.gamebreaker.tabladeanuncioskotlin.utils.CityHelper
import android.graphics.Bitmap
import android.net.Uri
import com.google.android.gms.tasks.OnCompleteListener
import ru.gamebreaker.tabladeanuncioskotlin.MainActivity
import ru.gamebreaker.tabladeanuncioskotlin.utils.ImagePicker
import ru.gamebreaker.tabladeanuncioskotlin.model.Ad
import ru.gamebreaker.tabladeanuncioskotlin.adapters.ImageAdapter
import ru.gamebreaker.tabladeanuncioskotlin.model.DbManager
import ru.gamebreaker.tabladeanuncioskotlin.fragments.FragmentCloseInterface
import ru.gamebreaker.tabladeanuncioskotlin.fragments.ImageListFragment
import java.io.ByteArrayOutputStream


class EditAdsAct : AppCompatActivity(), FragmentCloseInterface {

    var chooseImageFragment: ImageListFragment? = null
    lateinit var rootElement: ActivityEditAdsBinding
    private val dialog = DialogSpinnerHelper()
    lateinit var imageAdapter: ImageAdapter
    private val dbManager = DbManager()
    var editImagePos = 0
    private var imageIndex = 0
    private var isEditState = false
    private var ad: Ad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootElement = ActivityEditAdsBinding.inflate(layoutInflater)
        val view = rootElement.root
        setContentView(view)
        init()
        checkEditState()
    }

    private fun checkEditState(){
        isEditState = isEditState()
        if (isEditState){
            ad = intent.getSerializableExtra(MainActivity.ADS_DATA) as Ad
            if(ad != null) fillViews(ad!!)
        }
    }

    private fun isEditState(): Boolean{
        return intent.getBooleanExtra(MainActivity.EDIT_STATE, false)
    }

    private fun fillViews(ad: Ad) = with(rootElement) {
        tvFraction.text = ad.fraction
        tvHeroName.text = ad.heroName
        etTel.setText(ad.tel)
        etIndex.setText(ad.index)
        checkBoxWithSend.isChecked = ad.withSend.toBoolean()
        tvCategory.text = ad.category
        etTitle.setText(ad.title)
        etPrice.setText(ad.price)
        etDescription.setText(ad.description)
    }

    private fun init(){
        imageAdapter = ImageAdapter()
        rootElement.vpImages.adapter = imageAdapter
    }

    //OnClicks
    fun onClickSelectFraction(view: View){
        val listCountry = CityHelper.getAllCountries(this)
        dialog.showSpinnerDialog(this, listCountry, rootElement.tvFraction)
        if(rootElement.tvHeroName.text.toString() != getString(R.string.select_hero_name)){
            rootElement.tvHeroName.text = getString(R.string.select_hero_name)
        }
    }

    fun onClickSelectHeroName(view: View){
        val selectedCountry = rootElement.tvFraction.text.toString()
        if (selectedCountry != getString(R.string.select_fraction)){
            val listCity = CityHelper.getAllCities(selectedCountry,this)
            dialog.showSpinnerDialog(this, listCity, rootElement.tvHeroName)

        }else{
            Toast.makeText(this, getString(R.string.warning_no_fraction_selected), Toast.LENGTH_LONG).show()
        }
    }

    fun onClickSelectCategory(view: View){

         val listCategory = resources.getStringArray(R.array.category).toMutableList() as ArrayList
         dialog.showSpinnerDialog(this, listCategory, rootElement.tvCategory)


    }

    fun onClickGetImages(view: View){
        if(imageAdapter.mainArray.size == 0){
            ImagePicker.getMultiImages(this, 3)

        } else {

            openChooseItemFragment(null)
            chooseImageFragment?.updateAdapterFromEdit(imageAdapter.mainArray)

        }

    }

    fun onClickPublish(view: View){
        ad = fillAd()
        if(isEditState){
            ad?.copy(key = ad?.key)?.let {
                dbManager.publishAd(it, onPublishFinish()) } //обновляем существующее объявление
        } else {
            //dbManager.publishAd(adTemp, onPublishFinish()) //публикуем новое объявление
            uploadImages()
        }
    }

    private fun  onPublishFinish(): DbManager.FinishWorkListener{
        return object : DbManager.FinishWorkListener{
            override fun onFinish() {
                finish()
            }
        }
    }

    private fun fillAd(): Ad{
        val ad: Ad
        rootElement.apply{
            ad = Ad(
                tvFraction.text.toString(),
                tvHeroName.text.toString(),
                etTel.text.toString(),
                etIndex.text.toString(),
                checkBoxWithSend.isChecked.toString(),
                tvCategory.text.toString(),
                etPrice.text.toString(),
                etTitle.text.toString(),
                etDescription.text.toString(),
                "empty",
                "empty",
                "empty",
                dbManager.db.push().key,
                "0",
                dbManager.auth.uid
            )
        }
        return ad
    }

    override fun onFragmentClose(list : ArrayList<Bitmap>) {
        rootElement.scrollViewMain.visibility = View.VISIBLE
        imageAdapter.update(list)
        chooseImageFragment = null
    }

    fun openChooseItemFragment(newList: ArrayList<Uri>?){
        chooseImageFragment = ImageListFragment(this)
        if(newList != null)chooseImageFragment?.resizeSelectedImages(newList, true, this)
        rootElement.scrollViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.place_holder, chooseImageFragment!!)
        fm.commit()
    }

    private fun uploadImages(){
        if(imageAdapter.mainArray.size == imageIndex){
            dbManager.publishAd(ad!!, onPublishFinish())
            return
        }
        val byteArray = prepareImageByteArray(imageAdapter.mainArray[imageIndex])
        uploadImage(byteArray){
            dbManager
                .publishAd(
                    ad!!,
                    onPublishFinish()
                )
            nextImage(it.result.toString())
        }
    }

    private fun nextImage(uri: String){
        setImageUriToAd(uri)
        imageIndex++
        uploadImages()
    }

    private fun setImageUriToAd(uri: String){
        when(imageIndex){
            0 -> ad = ad?.copy(mainImage = uri)
            1 -> ad = ad?.copy(secondImage = uri)
            2 -> ad = ad?.copy(thirdImage = uri)
        }
    }

    private fun prepareImageByteArray(bitmap: Bitmap):ByteArray{
        val outStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, outStream)
        return outStream.toByteArray()
    }

    private fun uploadImage(byteArray: ByteArray, listener: OnCompleteListener<Uri>){
        val imStorageRef = dbManager.dbStorage
            .child(dbManager.auth.uid!!)
            .child("image_${System.currentTimeMillis()}")
        val upTask = imStorageRef.putBytes(byteArray)
        upTask.continueWithTask {
            task -> imStorageRef.downloadUrl
        }.addOnCompleteListener(listener)
    }
}
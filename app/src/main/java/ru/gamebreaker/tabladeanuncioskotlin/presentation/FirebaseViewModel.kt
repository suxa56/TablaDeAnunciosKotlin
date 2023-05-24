package ru.gamebreaker.tabladeanuncioskotlin.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.gamebreaker.tabladeanuncioskotlin.domain.model.Ad
import ru.gamebreaker.tabladeanuncioskotlin.data.DbManager

class FirebaseViewModel : ViewModel() {
    private val dbManager = DbManager()
    private val _liveAdsData = MutableLiveData<ArrayList<Ad>>()
    val liveAdsData: LiveData<ArrayList<Ad>> get() = _liveAdsData
    fun loadAllAds() {
        dbManager.getAllAds(object : DbManager.ReadDataCallback {
            override fun readData(list: ArrayList<Ad>) {
                _liveAdsData.value = list
            }
        })
    }

    fun loadAllAdsFromCat(cat: String) {
        dbManager.getAllAdsFromCat(cat, object : DbManager.ReadDataCallback {
            override fun readData(list: ArrayList<Ad>) {
                _liveAdsData.value = list
            }
        })
    }

    fun onFavClick(ad: Ad) {
        dbManager.onFavClick(ad, object : DbManager.FinishWorkListener {
            override fun onFinish(isDome: Boolean) {
                val updatedList = _liveAdsData.value
                val pos = updatedList?.indexOf(ad)
                if (pos != -1) {
                    pos?.let {
                        val favCounter =
                            if (ad.isFav) ad.favCounter.toInt() - 1 else ad.favCounter.toInt() + 1
                        updatedList[pos] = updatedList[pos].copy(
                            isFav = !ad.isFav,
                            favCounter = favCounter.toString()
                        )
                    }
                }
                _liveAdsData.postValue(updatedList!!)
            }
        })
    }


    fun adViewed(ad: Ad) {
        dbManager.adViewed(ad)
    }


    fun loadMyAds() {
        dbManager.getMyAds(object : DbManager.ReadDataCallback {
            override fun readData(list: ArrayList<Ad>) {
                _liveAdsData.value = list
            }
        })
    }

    fun loadMyFavs() {
        dbManager.getMyFavs(object : DbManager.ReadDataCallback {
            override fun readData(list: ArrayList<Ad>) {
                _liveAdsData.value = list
            }
        })
    }

    fun deleteItem(ad: Ad) {
        dbManager.deleteAd(ad, object : DbManager.FinishWorkListener {
            override fun onFinish(isDome: Boolean) {
                val updatedList = _liveAdsData.value
                updatedList?.remove(ad)
                _liveAdsData.postValue(updatedList!!)
            }
        })
    }
}
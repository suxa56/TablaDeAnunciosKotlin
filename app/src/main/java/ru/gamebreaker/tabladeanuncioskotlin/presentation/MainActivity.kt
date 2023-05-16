package ru.gamebreaker.tabladeanuncioskotlin.presentation

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import ru.gamebreaker.tabladeanuncioskotlin.R
import ru.gamebreaker.tabladeanuncioskotlin.data.AccountRepoImpl
import ru.gamebreaker.tabladeanuncioskotlin.databinding.ActivityMainBinding
import ru.gamebreaker.tabladeanuncioskotlin.domain.model.Ad
import ru.gamebreaker.tabladeanuncioskotlin.presentation.adapters.AdRVAdapter
import ru.gamebreaker.tabladeanuncioskotlin.presentation.dialoghelper.SignDialog

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    AdRVAdapter.Listener {
    private lateinit var tvAccount: TextView
    private lateinit var imAccount: ImageView
    private lateinit var binding: ActivityMainBinding
    private val signDialog = SignDialog(this)
    val mAuth = Firebase.auth
    val adapter = AdRVAdapter(this)
    private val firebaseViewModel: FirebaseViewModel by viewModels()
    private var clearUpdate: Boolean = true
    private var currentCategory: String? = null
    private var filterDb: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        init()
        initRecyclerView()
        initViewModel()
        bottomMenuOnClick()
    }

    override fun onStart() {
        super.onStart()
        uiUpdate(mAuth.currentUser)
    }

    override fun onResume() {
        super.onResume()
        binding.mainContent.botNavView.selectedItemId = R.id.id_home
    }

    private fun initViewModel() {
        firebaseViewModel.liveAdsData.observe(this) {
            val list = getAdsByCategory(it)
            adapter.submitList(list)
            binding.mainContent.tvEmpty.visibility =
                if (adapter.itemCount == 0) View.VISIBLE else View.GONE
        }
    }

    private fun getAdsByCategory(list: ArrayList<Ad>): ArrayList<Ad> {
        val tempList = ArrayList<Ad>()
        tempList.addAll(list)
        if (currentCategory != getString(R.string.def)) {
            tempList.clear()
            list.forEach {
                if (currentCategory == it.category) tempList.add(it)
            }
        }
        tempList.reverse()
        return tempList
    }

    private fun init() {
        currentCategory = getString(R.string.def)
        setSupportActionBar(binding.mainContent.toolbar) //указываем какой тулбар используется в активити (важно указать в начале)
        navViewSettings()
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.mainContent.toolbar,
            R.string.open,
            R.string.close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)
        tvAccount = binding.navView.getHeaderView(0).findViewById(R.id.tvAccountEmail)
        imAccount = binding.navView.getHeaderView(0).findViewById(R.id.imAccountImage)

    }

    private fun bottomMenuOnClick() = with(binding) {
        mainContent.botNavView.setOnItemSelectedListener { item ->
            clearUpdate = true
            when (item.itemId) {
                R.id.id_new_ad -> {
                    if (!mAuth.currentUser!!.isAnonymous) {
                        val i = Intent(
                            this@MainActivity,
                            EditAdsAct::class.java
                        ) //передаём контекст на котором находимся и активити на которое хотим перейти
                        startActivity(i) //запускаем интент и новое активити
                    } else {
                        signDialog.createSignDialog(SignDialog.SIGN_UP_STATE)
                        binding.mainContent.botNavView.selectedItemId = R.id.id_home
                    }
                }
                R.id.id_my_ads -> {
                    if (!mAuth.currentUser!!.isAnonymous) {
                        firebaseViewModel.loadMyAds()
                        mainContent.toolbar.title = getString(R.string.ad_my_ads)
                    } else {
                        signDialog.createSignDialog(SignDialog.SIGN_UP_STATE)
                        binding.mainContent.botNavView.selectedItemId = R.id.id_home
                    }
                }
                R.id.id_favorites -> {
                    if (!mAuth.currentUser!!.isAnonymous) {

                        firebaseViewModel.loadMyFavs()
                        mainContent.toolbar.title = "Избранное"
                    } else {
                        signDialog.createSignDialog(SignDialog.SIGN_UP_STATE)
                        binding.mainContent.botNavView.selectedItemId = R.id.id_home
                    }
                }
                R.id.id_home -> {
                    currentCategory = getString(R.string.def)
                    filterDb?.let { firebaseViewModel.loadAllAds() }
                    mainContent.toolbar.title = getString(R.string.all_ads)
                }
            }
            true
        }
    }

    private fun initRecyclerView() {
        binding.apply {
            mainContent.rcView.layoutManager = LinearLayoutManager(this@MainActivity)
            mainContent.rcView.adapter = adapter
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean = with(binding) {
        clearUpdate = true
        val length = Toast.LENGTH_SHORT
        when (item.itemId) {
            R.id.id_auto -> {
                getAdsFromCat(R.string.ad_auto)
                mainContent.toolbar.title = getString(R.string.ad_auto)
            }
            R.id.id_device -> {
                getAdsFromCat(R.string.ad_device)
                mainContent.toolbar.title = getString(R.string.ad_device)
            }
            R.id.id_child -> {
                getAdsFromCat(R.string.ad_child)
                mainContent.toolbar.title = getString(R.string.ad_child)
            }
            R.id.id_house -> {
                getAdsFromCat(R.string.ad_house)
                mainContent.toolbar.title = getString(R.string.ad_house)
            }
            R.id.id_service -> {
                getAdsFromCat(R.string.ad_service)
                mainContent.toolbar.title = getString(R.string.ad_service)
            }
            R.id.id_work -> {
                getAdsFromCat(R.string.ad_work)
                mainContent.toolbar.title = getString(R.string.ad_work)
            }
            R.id.id_pet -> {
                getAdsFromCat(R.string.ad_pet)
                mainContent.toolbar.title = getString(R.string.ad_pet)
            }
            R.id.id_sport -> {
                getAdsFromCat(R.string.ad_sport)
                mainContent.toolbar.title = getString(R.string.ad_sport)
            }
            R.id.id_my_ads -> {
                firebaseViewModel.loadMyAds()
                mainContent.toolbar.title = getString(R.string.ad_my_ads)
            }
            R.id.all_ads -> {
                currentCategory = getString(R.string.def)
                filterDb?.let { firebaseViewModel.loadAllAds() }
                mainContent.toolbar.title = getString(R.string.all_ads)
            }
            R.id.id_sign_up -> {
                signDialog.createSignDialog(SignDialog.SIGN_IN_STATE)
            }
            R.id.id_sign_in -> {
                signDialog.createSignDialog(SignDialog.SIGN_IN_STATE)
            }
            R.id.id_sign_out -> {
                val text = getString(R.string.sign_out_done)
                Toast.makeText(this@MainActivity, text, length).show()
                uiUpdate(null)
                mAuth.signOut()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun getAdsFromCat(cat: Int) {
        val catForDB: Int = when (cat) {
            R.string.ad_auto -> R.string.ad_heroes
            R.string.ad_device -> R.string.ad_faction_war
            R.string.ad_child -> R.string.ad_arena
            R.string.ad_house -> R.string.ad_dungeons
            R.string.ad_service -> R.string.ad_cb
            R.string.ad_work -> R.string.ad_tower
            R.string.ad_pet -> R.string.lf_clan
            R.string.ad_sport -> R.string.lf_members
            else -> {
                R.string.def
            }
        }
        currentCategory = getString(catForDB)
        filterDb?.let { firebaseViewModel.loadAllAdsFromCat(getString(catForDB)) }
    }

    fun uiUpdate(user: FirebaseUser?) {
        imAccount.setImageResource(R.drawable.ic_account_default)
        if (user == null) {
            signDialog.accHelper.signInAnonymously(object : AccountRepoImpl.Listener {
                override fun onComplete() {
                    tvAccount.text = getString(R.string.the_guest)
                    binding.navView.menu.findItem(R.id.id_my_ads).isVisible = false
                    binding.navView.menu.findItem(R.id.id_sign_in).isVisible = true
                    binding.navView.menu.findItem(R.id.id_sign_up).isVisible = true
                    binding.navView.menu.findItem(R.id.id_sign_out).isVisible = false
                }
            })
        } else if (!user.isAnonymous) {
            tvAccount.text = user.email
            binding.navView.menu.findItem(R.id.id_my_ads).isVisible = true
            binding.navView.menu.findItem(R.id.id_sign_in).isVisible = false
            binding.navView.menu.findItem(R.id.id_sign_up).isVisible = false
            binding.navView.menu.findItem(R.id.id_sign_out).isVisible = true
        } else {
            tvAccount.text = getString(R.string.the_guest)
            binding.navView.menu.findItem(R.id.id_my_ads).isVisible = false
            binding.navView.menu.findItem(R.id.id_sign_in).isVisible = true
            binding.navView.menu.findItem(R.id.id_sign_up).isVisible = true
            binding.navView.menu.findItem(R.id.id_sign_out).isVisible = false
        }
    }

    override fun onDeleteItem(ad: Ad) {
        firebaseViewModel.deleteItem(ad)
    }

    override fun onAdViewed(ad: Ad) {
        firebaseViewModel.adViewed(ad)
        val i = Intent(this, DescriptionActivity::class.java)
        i.putExtra("AD", ad)
        startActivity(i)
    }

    override fun onFavClicked(ad: Ad) {
        firebaseViewModel.onFavClick(ad)
    }

    //изменение цвета текста категорий из выдвижного меню
    private fun navViewSettings() = with(binding) {
        val menu = navView.menu
        val adsCategory = menu.findItem(R.id.adsCat)
        val adsCategory2 = menu.findItem(R.id.adsCat2)
        val accCategory = menu.findItem(R.id.accCat)
        val spanAdsCat = SpannableString(adsCategory.title)
        val spanAdsCat2 = SpannableString(adsCategory2.title)
        val spanAccCat = SpannableString(accCategory.title)
        spanAdsCat.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    this@MainActivity,
                    R.color.onPrimaryContainer
                )
            ), 0, adsCategory.title!!.length, 0
        )
        spanAdsCat2.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    this@MainActivity,
                    R.color.onPrimaryContainer
                )
            ), 0, adsCategory2.title!!.length, 0
        )
        spanAccCat.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    this@MainActivity,
                    R.color.onPrimaryContainer
                )
            ), 0, accCategory.title!!.length, 0
        )
        adsCategory.title = spanAdsCat
        adsCategory2.title = spanAdsCat2
        accCategory.title = spanAccCat
    }

    companion object {
        const val EDIT_STATE = "edit_state"
        const val ADS_DATA = "ads_data"
    }
}
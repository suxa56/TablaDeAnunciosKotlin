package ru.gamebreaker.tabladeanuncioskotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import ru.gamebreaker.tabladeanuncioskotlin.databinding.ActivityMainBinding
import ru.gamebreaker.tabladeanuncioskotlin.dialoghelper.DialogConst
import ru.gamebreaker.tabladeanuncioskotlin.dialoghelper.DialogHelper

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var tvAccount:TextView
    private lateinit var rootElement:ActivityMainBinding
    private val dialogHelper = DialogHelper(this)
    val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootElement = ActivityMainBinding.inflate(layoutInflater)
        val view = rootElement.root
        setContentView(view)
        init()
    }

    override fun onStart() {
        super.onStart()
        uiUpdate(mAuth.currentUser)
    }

    private fun init(){

        val toggle = ActionBarDrawerToggle(this, rootElement.drawerLayout, rootElement.mainContent.toolbar, R.string.open, R.string.close)
        rootElement.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        rootElement.navView.setNavigationItemSelectedListener(this)
        tvAccount = rootElement.navView.getHeaderView(0).findViewById(R.id.tvAccountEmail)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        val length = Toast.LENGTH_SHORT
        val textAddToast = getString(R.string.textAddToast)

        when(item.itemId){

            R.id.id_my_ads ->{

                val text = textAddToast + getString(R.string.ad_my_ads)
                Toast.makeText(this, text, length).show()

            }
            R.id.id_car ->{

                val text = textAddToast + getString(R.string.ad_car)
                Toast.makeText(this, text, length).show()

            }
            R.id.id_pc ->{

                val text = textAddToast + getString(R.string.ad_pc)
                Toast.makeText(this, text, length).show()

            }
            R.id.id_smart ->{

                val text = textAddToast + getString(R.string.ad_smartphone)
                Toast.makeText(this, text, length).show()

            }
            R.id.id_dm ->{

                val text = textAddToast + getString(R.string.ad_dm)
                Toast.makeText(this, text, length).show()

            }
            R.id.id_sign_up ->{

                val text = textAddToast + getString(R.string.ac_sign_up)
                Toast.makeText(this, text, length).show()

                dialogHelper.createSignDialog(DialogConst.SIGN_UP_STATE)

            }
            R.id.id_sign_in ->{

                val text = textAddToast + getString(R.string.ac_sign_in)
                Toast.makeText(this, text, length).show()

                dialogHelper.createSignDialog(DialogConst.SIGN_IN_STATE)

            }
            R.id.id_sign_out ->{

                val text = getString(R.string.sign_out_done)
                Toast.makeText(this, text, length).show()

                uiUpdate(null)
                mAuth.signOut()

            }

        }

        rootElement.drawerLayout.closeDrawer(GravityCompat.START)
        return true

    }

    fun uiUpdate(user:FirebaseUser?){
        tvAccount.text = if (user == null){
            resources.getString(R.string.not_reg)
        }else{
            user.email
        }
    }

}
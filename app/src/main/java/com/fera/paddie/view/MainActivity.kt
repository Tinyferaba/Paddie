package com.fera.paddie.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.fera.paddie.R
import com.fera.paddie.view.aboutUs.AboutUsActivity
import com.fera.paddie.view.uploadToCloud.UploadToCloudActivity
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var sideDrawer: NavigationView
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainDrawerLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        addActionListeners()

        setStatusBarColor()
    }

    private fun addActionListeners() {
        sideDrawer.setNavigationItemSelectedListener {menuItem ->
            when(menuItem.itemId){
                R.id.menuAboutUs -> {
                    val intent = Intent(this, AboutUsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menuUploadToCloud -> {
                    val intent = Intent(this, UploadToCloudActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> {true}
            }
        }
    }

    private fun initViews() {
        sideDrawer = findViewById(R.id.mainSideDrawer)
        drawerLayout = findViewById(R.id.mainDrawerLayout)

    }

    fun showHideSideDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
        else
            drawerLayout.openDrawer(GravityCompat.START)
    }
    
    private fun setStatusBarColor(){
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
    }
}
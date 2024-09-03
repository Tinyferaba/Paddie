package com.fera.paddie.view.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
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
                    drawerLayout.closeDrawer(GravityCompat.START)
                    val intent = Intent(this, AboutUsActivity::class.java)
                    startActivity(intent)
                    true
                }
//                R.id.menuUploadToCloud -> {
//                    drawerLayout.closeDrawer(GravityCompat.START)
//                    val intent = Intent(this, UploadToCloudActivity::class.java)
//                    startActivity(intent)
//                    true
//                }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = window.decorView
            decorView.systemUiVisibility = decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }

    override fun onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
            showHideSideDrawer()
        else
            super.onBackPressed()
    }
}
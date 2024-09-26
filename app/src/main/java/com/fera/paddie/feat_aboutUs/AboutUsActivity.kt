package com.fera.paddie.feat_aboutUs

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fera.paddie.R
import com.fera.paddie.common.controller.DeveloperController
import com.fera.paddie.common.model.TblDevelopers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AboutUsActivity : AppCompatActivity() {
    private val TAG = "AboutUsActivity"

    //######### VIEW #########//
    private lateinit var rvDevelopers: RecyclerView
    private lateinit var adapterDevelopers: AdapterDevelopers
    private lateinit var developerController: DeveloperController
    private var developerList = emptyList<TblDevelopers>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_about_us)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()

        setStatusBarColor()
    }

    private fun initViews() {
        developerController = ViewModelProvider(this)[DeveloperController::class.java]

        rvDevelopers = findViewById(R.id.rvDeveloper_aboutUs)
        rvDevelopers.layoutManager = LinearLayoutManager(this)


        CoroutineScope(Dispatchers.IO).launch {
            developerList = developerController.getAllDevelopers()

            withContext(Dispatchers.Main){
                adapterDevelopers = AdapterDevelopers(this@AboutUsActivity, developerList)
                rvDevelopers.adapter = adapterDevelopers
            }
        }
    }

    private fun setStatusBarColor(){
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = window.decorView
            decorView.systemUiVisibility = decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }
}
package com.fera.paddie.view.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fera.paddie.R
import com.fera.paddie.auth.LoginAndSignUp
import com.fera.paddie.controller.NoteControllers
import com.fera.paddie.model.TblNote
import com.fera.paddie.model.util.CONST
import com.fera.paddie.view.aboutUs.AboutUsActivity
import com.fera.paddie.view.main.addNote.AddNoteActivity
import com.fera.paddie.view.uploadToCloud.UploadToCloudActivity
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), AdapterNoteList.NoteActivities {
    private val TAG = "MainActivity"

    private lateinit var sideNavigation: NavigationView
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var ivSearch: ImageView
    private lateinit var ivClearSearchField: ImageView
    private lateinit var ivAddNote: ImageView //Buttons
    private lateinit var edtSearchField: EditText
    private lateinit var ivShowSideDrawer: ImageView

    private lateinit var tvLogin: TextView
    private lateinit var sIvProfilePhoto: ShapeableImageView
    private lateinit var tvName: TextView
    private lateinit var tvMail: TextView

    //######### NOTEs & TODOs List PROPERTY #########//
    private lateinit var rvNoteList: RecyclerView
    private lateinit var adapterNoteList: AdapterNoteList
    private var noteList = mutableListOf<TblNote>()

    //######### CONTROLLERS PROPERTY #########//
    private lateinit var noteControllers: NoteControllers
    private lateinit var mDBRef: DatabaseReference

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
        sideNavigation.setNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.menuAboutUs -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    val intent = Intent(this, AboutUsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menuUploadToCloud -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    val intent = Intent(this, UploadToCloudActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> {true}
            }
        }
        tvLogin.setOnClickListener {
            val intent = Intent(this, LoginAndSignUp::class.java)
            startActivity(intent)
        }
        ivAddNote.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            startActivity(intent)
        }
        ivShowSideDrawer.setOnClickListener {
            showHideSideDrawer()
        }

        ivClearSearchField.setOnClickListener { clearSearchField() }
        ivSearch.setOnClickListener { searchNotes() }
        edtSearchField.addTextChangedListener { searchNotes() }
    }

    private fun initViews() {
        sideNavigation = findViewById(R.id.sideNavigation)
        drawerLayout = findViewById(R.id.mainDrawerLayout)

        mDBRef = FirebaseDatabase.getInstance().getReference()

        //######### VIEWS #########//
        ivSearch = findViewById(R.id.ivSearchNoteAndTodo)     //Image Views
        ivClearSearchField = findViewById(R.id.ivClearSearchField)
        ivAddNote = findViewById(R.id.ivAddNote)
        edtSearchField =findViewById(R.id.edtSearchField)
        ivShowSideDrawer = findViewById(R.id.ivShowSideDrawer)

        tvLogin = sideNavigation.getHeaderView(0).findViewById(R.id.tvLogin_sideDrawer)
        sIvProfilePhoto = sideNavigation.getHeaderView(0).findViewById(R.id.sIvProfilePhoto_sideDrawer)
        tvName = sideNavigation.getHeaderView(0).findViewById(R.id.tvName_sideDrawer)
        tvMail = sideNavigation.getHeaderView(0).findViewById(R.id.tvMail_sideDrawer)

        //######### CONTROLLERS #########//
        noteControllers = NoteControllers(application)

        //######### RECYCLER VIEWS #########//
        rvNoteList =findViewById(R.id.rvNoteList_home)
        rvNoteList.layoutManager = LinearLayoutManager(this)
        noteControllers.allNotes.observe(this) {noteList ->
            adapterNoteList = AdapterNoteList(this, noteList, this)
            rvNoteList.adapter = adapterNoteList
        }
    }


    override fun navigateToAddNoteFragment(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val tblNote = getNote(id)

            withContext(Dispatchers.Main){
                val intent = Intent(baseContext, AddNoteActivity::class.java)
                intent.putExtra(CONST.KEY_TBL_NOTE, tblNote)
                startActivity(intent)
            }
        }
    }

    private fun searchNotes() {
        if (edtSearchField.text.isEmpty()){
            noteControllers.allNotes.observe(this) {noteList ->
                adapterNoteList.updateNoteList(noteList)
            }
        } else {
            val searchText = edtSearchField.text.toString()

            CoroutineScope(Dispatchers.IO).launch {
                noteList.clear()
                noteList.addAll(noteControllers.searchNotes(searchText))
                withContext(Dispatchers.Main){
                    adapterNoteList.updateNoteList(noteList)
                    adapterNoteList.notifyDataSetChanged()
                }
            }
        }
    }

    private fun clearSearchField() {
        edtSearchField.setText("")
    }

    override fun updateNote(tblNote: TblNote) {
        CoroutineScope(Dispatchers.IO).launch {
            tblNote.updated = true
            noteControllers.updateNote(tblNote)
        }
    }

    override fun deleteNote(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            noteControllers.deleteNote(id)
        }
    }

    override fun updateFavourite(id: Int, favourite: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            noteControllers.updateFavourite(id, favourite)
        }
    }

    private suspend fun getNote(id: Int): TblNote {
        return withContext(Dispatchers.IO){
            noteControllers.getNote(id)
        }
    }


    private fun showHideSideDrawer() {
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
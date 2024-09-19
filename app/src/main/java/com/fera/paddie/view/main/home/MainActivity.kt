package com.fera.paddie.view.main.home

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fera.paddie.R
import com.fera.paddie.auth.LoginAndSignUp
import com.fera.paddie.controller.DeveloperController
import com.fera.paddie.controller.NoteControllers
import com.fera.paddie.model.TblDevelopers
import com.fera.paddie.model.TblNote
import com.fera.paddie.model.TblUser
import com.fera.paddie.model.util.CONST
import com.fera.paddie.view.aboutUs.AboutUsActivity
import com.fera.paddie.view.main.addNote.AddNoteActivity
import com.fera.paddie.view.uploadToCloud.UploadToCloudActivity
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), AdapterNoteList.NoteActivities {
    private val TAG = "MainActivity"

    //######### CONST #########//
    private var loggedIn = false

    //######### VIEWS #########//
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

        loadUserData()

//        lifecycleScope.launch {
//            loadDemoData()
//        }
    }

    private fun loadUserData() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid != null){
            tvLogin.text = "Logout"
            loggedIn = true

            mDBRef.child(CONST.fDB_DIR_USER).child(uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        val user = task.result.getValue(TblUser::class.java)

                        if (user != null){
                            val name = "${user.firstName} ${user.lastName}"
                            val email = user.email

                            tvMail.text = email
                            tvName.text = name
                            user.photo?.let { photoURI ->
                                Glide.with(this)
                                    .load(photoURI)
                                    .placeholder(R.drawable.kamake)
                                    .centerCrop()
                                    .into(sIvProfilePhoto)
                            }
                            Log.d(TAG, "loadUserData: ${user.photo}")
                        }
                    } else {
                        Toast.makeText(this, "Error loading User data: ${task.exception}: ", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private suspend fun loadDemoData() {
        val photo1 = ContextCompat.getDrawable(this, R.drawable.image_1)
        val photo2 = ContextCompat.getDrawable(this, R.drawable.image_2)
        val photo3 = ContextCompat.getDrawable(this, R.drawable.image_3)
        val photo4 = ContextCompat.getDrawable(this, R.drawable.image_4)

        val devList = listOf(
            TblDevelopers("22302067", "Noody", "HAGAYO", "22302067noha@student.pnguot.ac.pg", getBitmap(photo1!!)),
            TblDevelopers("22302074", "Boi", "KOBLA", "22302074boko@student.pnguot.ac.pg", getBitmap(photo2!!)),
            TblDevelopers("22301822", "Shima Delilah", "DENSON", "22301822shde@student.pnguot.ac.pg", getBitmap(photo3!!)),
            TblDevelopers("22301788", "Renee", "ATTMANKIA", "22301788reat@student.pnguot.ac.pg", getBitmap(photo4!!))
        )

        val uploadScope = CoroutineScope(Dispatchers.IO)
        val devControllers = ViewModelProvider(this)[DeveloperController::class.java]

        val uploadJob = devList.map { tblDevelopers ->
            uploadScope.launch {
                devControllers.insertDeveloper(tblDevelopers)
            }
        }
        uploadJob.forEach { job -> job.join()}

    }

    private fun getBitmap(drawable: Drawable): Bitmap {
        return (drawable as BitmapDrawable).bitmap
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
            if (loggedIn){
                logoutUser()
            } else {
                val intent = Intent(this, LoginAndSignUp::class.java)
                startActivity(intent)
            }

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

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        loggedIn = false
        tvLogin.text = "Login"
        sIvProfilePhoto.setImageResource(R.drawable.kamake)
        tvName.text = null
        tvMail.text = null
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
package com.fera.paddie.view.main.home

import android.animation.ArgbEvaluator
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
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
import androidx.recyclerview.widget.ItemTouchHelper
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
import com.fera.paddie.util.CONST
import com.fera.paddie.view.Initializer
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), AdapterNoteList.NoteActivities {
    private val TAG = "MainActivity"

    //######### CONST #########//
    private var loggedIn = false
    private var mode = MODE.ADD

    //######### VIEWS #########//
    private lateinit var sideNavigation: NavigationView
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var ivSearch: ImageView
    private lateinit var ivClearSearchField: ImageView
    private lateinit var ivAddNote: ImageView //Buttons
    private lateinit var edtSearchField: EditText
    private lateinit var ivShowSideDrawer: ImageView

    private lateinit var llCheckBox: LinearLayout
    private lateinit var chkbxSelectAll: CheckBox

    private lateinit var tvLogin: TextView
    private lateinit var sIvProfilePhoto: ShapeableImageView
    private lateinit var tvName: TextView
    private lateinit var tvMail: TextView

    //######### NOTEs & TODOs List PROPERTY #########//
    private lateinit var rvNoteList: RecyclerView
    private lateinit var adapterNoteList: AdapterNoteList
    private var noteList = mutableListOf<TblNote>()
    private var noteListDelete = mutableListOf<TblNote>()

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

//        Initializer.initApp(this)

//        lifecycleScope.launch {
//            loadDemoData()
//        }
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
                    intent.putExtra("uploadToCloud", true)
                    startActivity(intent)
                    true
                }
                R.id.menuDownloadFromCloud -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    val intent = Intent(this, UploadToCloudActivity::class.java)
                    intent.putExtra("uploadToCloud", false)
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
                CONST.uploadNotes = false
                startActivity(intent)
            }

        }
        ivAddNote.setOnClickListener {
            if (mode == MODE.ADD){
                val intent = Intent(this, AddNoteActivity::class.java)
                startActivity(intent)
            } else if (mode == MODE.DELETE){
                deleteSelectedNotes()
            }
        }
        ivShowSideDrawer.setOnClickListener {
            showHideSideDrawer()
        }

        chkbxSelectAll.setOnClickListener {
            mode = MODE.DELETE
            val checked = chkbxSelectAll.isChecked
            noteListDelete.clear()

            if (checked) {
//                ivUploadToCloud.visibility = View.VISIBLE
                adapterNoteList.checkAll(true)
            } else {
//                ivUploadToCloud.visibility = View.GONE
                adapterNoteList.checkAll(false)
            }
        }

        ivClearSearchField.setOnClickListener { clearSearchField() }
        ivSearch.setOnClickListener { searchNotes() }
        edtSearchField.addTextChangedListener { searchNotes() }
    }

    private fun deleteSelectedNotes() {
        val job = noteListDelete.map { tblNote ->
            CoroutineScope(Dispatchers.IO).launch {
                noteControllers.deleteNote(tblNote.pkNoteId!!)
            }
        }

        CoroutineScope(Dispatchers.Main).launch{
            job.joinAll()
            Toast.makeText(this@MainActivity, "Deleted", Toast.LENGTH_SHORT).show()

            noteListDelete.clear()
            if (noteListDelete.isEmpty())
                changeMode(MODE.ADD)
            adapterNoteList.notifyDataSetChanged()
        }
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

        llCheckBox = findViewById(R.id.lLCheckBox)
        chkbxSelectAll = findViewById(R.id.chkbxSelectAll_main)

        tvLogin = sideNavigation.getHeaderView(0).findViewById(R.id.tvLogin_sideDrawer)
        sIvProfilePhoto = sideNavigation.getHeaderView(0).findViewById(R.id.sIvProfilePhoto_sideDrawer)
        tvName = sideNavigation.getHeaderView(0).findViewById(R.id.tvName_sideDrawer)
        tvMail = sideNavigation.getHeaderView(0).findViewById(R.id.tvMail_sideDrawer)

        //######### CONTROLLERS #########//
        noteControllers = NoteControllers(application)

        //######### RECYCLER VIEWS #########//
        rvNoteList =findViewById(R.id.rvNoteList_home)
        rvNoteList.layoutManager = LinearLayoutManager(this)
        adapterNoteList = AdapterNoteList(this, noteList, this)
        rvNoteList.adapter = adapterNoteList

        noteControllers.allNotes.observe(this) {noteList ->
            if (noteList.isEmpty()){
                llCheckBox.visibility = View.GONE
                chkbxSelectAll.isChecked = false
            }
            adapterNoteList.updateNoteList(noteList)
        }

        setupSwipeToDelete(rvNoteList, adapterNoteList)
    }


    override fun navigateToAddNoteFragment(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val tblNote = getNote(id)

            withContext(Dispatchers.Main){
                val intent = Intent(baseContext, AddNoteActivity::class.java)
                intent.putExtra(CONST.KEY_TBL_NOTE, tblNote)
                startActivity(intent)
                if (mode == MODE.DELETE){
                    mode = MODE.ADD
                    changeMode(mode)
                }
            }
        }
    }

    override fun addToDeleteList(noteList: List<TblNote>) {
        noteListDelete.clear()
        noteListDelete.addAll(noteList)

        if (noteListDelete.isNotEmpty())
            if (mode == MODE.ADD)
                changeMode(MODE.DELETE)
    }

    override fun addToDeleteList(note: TblNote) {
        noteListDelete.add(note)
        if (noteListDelete.isNotEmpty())
            if (mode == MODE.ADD)
                changeMode(MODE.DELETE)
    }

    override fun clearDeleteList() {
        noteListDelete.clear()

        if (noteListDelete.isNotEmpty())
            changeMode(MODE.DELETE)
    }

    override fun changeMode(mode: MODE) {
        this.mode = mode
        if (mode == MODE.DELETE){
            ivAddNote.setImageResource(R.drawable.ic_delete)
            llCheckBox.visibility = View.VISIBLE
        } else if (mode == MODE.ADD) {
            ivAddNote.setImageResource(R.drawable.ic_add)
        }
    }

    override fun removeFromDeletedList(tblNote: TblNote) {
        noteListDelete.remove(tblNote)
        if (noteListDelete.isEmpty())
            if (mode == MODE.DELETE)
                changeMode(MODE.ADD)
    }

//    private fun hideViewsOnEmptyList(listIsEmpty: Boolean){
//        if (listIsEmpty){
//            ivAddNote.setImageResource(R.drawable.ic_add)
//        } else {
//            ivAddNote.setImageResource(R.drawable.ic_delete)
//        }
//    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        loggedIn = false
        tvLogin.text = "Login"
        sIvProfilePhoto.setImageResource(R.drawable.kamake)
        tvName.text = null
        tvMail.text = null
    }

    private fun loadUserData() {
        val accountCreated = intent.getBooleanExtra("accountCreated", false)
        if (accountCreated){
            var uid = FirebaseAuth.getInstance().uid
            CoroutineScope(Dispatchers.IO).launch {
                do {
                    if (uid != null){
                        tvLogin.text = "Logout"
                        loggedIn = true

                        mDBRef.child(CONST.fDB_DIR_USER).child(uid!!)
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
                                            Glide.with(this@MainActivity)
                                                .load(photoURI)
                                                .placeholder(R.drawable.kamake)
                                                .centerCrop()
                                                .into(sIvProfilePhoto)
                                        }
                                        Log.d(TAG, "loadUserData: ${user.photo}")
                                    }
                                } else {
                                    Toast.makeText(this@MainActivity, "Error loading User data: ${task.exception}: ", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                    delay(100)
                    uid = FirebaseAuth.getInstance().uid
                } while (uid == null)
            }
        }
    }


    private fun getBitmap(drawable: Drawable): Bitmap {
        return (drawable as BitmapDrawable).bitmap
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
        else {
            if(mode == MODE.DELETE){
                mode = MODE.ADD
                changeMode(MODE.ADD)
                noteListDelete.clear()
                adapterNoteList.hideCheckBox()
            } else {
                super.onBackPressed()
            }
        }
    }

    private fun setupSwipeToDelete(recyclerView: RecyclerView, adapter: AdapterNoteList) {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            // onMove is for drag & drop, which we don't need here
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            // Called when an item is swiped
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val noteToDelete = adapter.noteList[position]

                // Call the parent's deleteNote method
                adapter.parentAct.deleteNote(noteToDelete.pkNoteId!!)

                // Remove the item from the adapter's list and notify the adapter
                adapter.noteList = adapter.noteList.toMutableList().also {
                    it.removeAt(position)
                }
                adapter.notifyItemRemoved(position)
            }

            // Optional: Customize swipe background (red background with delete icon)
            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                // Customize the swipe background and icon (optional)
                if (dX == 0.0f){
                    viewHolder.itemView.setBackgroundResource(R.drawable.bg_list_item)
                } else {
                    val colorObtained = getColorFromValue(this@MainActivity, dX.toInt())
                    viewHolder.itemView.backgroundTintList = ColorStateList.valueOf(colorObtained)
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    fun getColorFromValue(context: Context, value: Int): Int {
        // Clamp the value between 0 and -720
        val clampedValue = value.coerceIn(-720, 0)

        // Define the color range from white (start) to black (end)
        val startColor = ContextCompat.getColor(context, android.R.color.white)
        val endColor = ContextCompat.getColor(context, android.R.color.black)

        // Calculate the fraction based on the clamped value
        // White to Black transition happens between 0 to -270
        val fraction = when {
            clampedValue >= -270 -> (clampedValue - 0f) / (-270f)  // 0 to -270 range
            else -> 1f  // For values between -271 to -720, it's fully black
        }

        // Return the interpolated color
        return ArgbEvaluator().evaluate(fraction, startColor, endColor) as Int
    }
}
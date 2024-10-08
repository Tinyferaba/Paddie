package com.fera.paddie.main

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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fera.paddie.R
import com.fera.paddie.feat_auth.LoginAndSignUp
import com.fera.paddie.common.controller.NoteControllers
import com.fera.paddie.common.controller.UserController
import com.fera.paddie.common.model.TblNote
import com.fera.paddie.common.model.TblUser
import com.fera.paddie.util.CONST
import com.fera.paddie.util.SortOrderType
import com.fera.paddie.feat_aboutUs.AboutUsActivity
import com.fera.paddie.feat_addNote.AddNoteActivity
import com.fera.paddie.feat_uploadToCloud.UploadToCloudActivity
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), AdapterNoteList.NoteActivities {
    private val TAG = "MainActivity"

    //######### CONST #########//
    private var loggedIn = false
    private var modeMain = ModeMain.ADD
    private var viewFavourites = false

    private var currentSortType = SortOrderType.TITLE_DESC     // Sort Order
    private var nextSortType = SortOrderType.DESCRIPTION_ASC     // Sort Order

    //######### VIEWS #########//
    private lateinit var sideNavigation: NavigationView
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var ivSearch: ImageView            // Search Options
    private lateinit var ivClearSearchField: ImageView
    private lateinit var ivAddNote: ImageView
    private lateinit var edtSearchField: EditText
    private lateinit var ivShowSideDrawer: ImageView

    private lateinit var clSortOptions: ConstraintLayout               // Sort Options
    private lateinit var ivShowHideSortOption: ImageView
    private lateinit var ivSortBy: ImageView
    private lateinit var ivFavourites: ImageView

    private lateinit var llCheckBox: LinearLayout   //  Check box
    private lateinit var chkbxSelectAll: CheckBox

    private lateinit var tvLogin: TextView      //  Side Drawer
    private lateinit var sIvProfilePhoto: ShapeableImageView
    private lateinit var tvName: TextView
    private lateinit var tvMail: TextView

    //######### NOTEs & TODOs List PROPERTY #########//
    private lateinit var rvNoteList: RecyclerView
    private lateinit var adapterNoteList: AdapterNoteList
    private var listByTitle = emptyList<TblNote>()
    private var noteListDelete = mutableListOf<TblNote>()

    //######### CONTROLLERS PROPERTY #########//
    private lateinit var userController: UserController
    private lateinit var noteController: NoteControllers
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
    }


    private fun addActionListeners() {
        sideNavigation.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menuAboutUs -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    val intent = Intent(this, AboutUsActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.menuCloud -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    val intent = Intent(this, UploadToCloudActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> {
                    true
                }
            }
        }
        tvLogin.setOnClickListener {
            if (loggedIn) {
                logoutUser()
            } else {
                val intent = Intent(this, LoginAndSignUp::class.java)
                CONST.uploadNotes = false
                startActivity(intent)
            }

        }
        ivShowHideSortOption.setOnClickListener {
            showHideSortOptions(clSortOptions.isVisible)
        }
        ivSortBy.setOnClickListener {
            changeSortOrder()
        }
        ivFavourites.setOnClickListener {
            viewFavourites = !viewFavourites
            if (viewFavourites) {
                ivFavourites.setImageResource(R.drawable.ic_favourite)
            } else {
                ivFavourites.setImageResource(R.drawable.ic_unfavourite)
            }
            sortNotes()
        }
        ivAddNote.setOnClickListener {
            if (modeMain == ModeMain.ADD) {
                val intent = Intent(this, AddNoteActivity::class.java)
                startActivity(intent)
            } else if (modeMain == ModeMain.DELETE) {
                deleteSelectedNotes()
            }
        }
        ivShowSideDrawer.setOnClickListener {
            showHideSideDrawer()
        }

        chkbxSelectAll.setOnClickListener {
            modeMain = ModeMain.DELETE
            val checked = chkbxSelectAll.isChecked

            noteListDelete.clear()

            if (checked) {
                adapterNoteList.checkAll(true)
            } else {
                adapterNoteList.checkAll(false)
            }
        }

        ivClearSearchField.setOnClickListener { clearSearchField() }
        ivSearch.setOnClickListener { searchNotes() }
        edtSearchField.addTextChangedListener { searchNotes() }
    }

    private fun changeSortOrder() {
        when (nextSortType) {
            SortOrderType.TITLE_ASC -> {
                ivSortBy.setImageResource(R.drawable.ic_sort_title_asc)
                currentSortType = nextSortType
                sortNotes()
                nextSortType = SortOrderType.TITLE_DESC
            }

            SortOrderType.TITLE_DESC -> {
                ivSortBy.setImageResource(R.drawable.ic_sort_title_desc)
                currentSortType = nextSortType
                sortNotes()
                nextSortType = SortOrderType.DESCRIPTION_ASC
            }

            SortOrderType.DESCRIPTION_ASC -> {
                ivSortBy.setImageResource(R.drawable.ic_sort_desc_asc)
                currentSortType = nextSortType
                sortNotes()
                nextSortType = SortOrderType.DESCRIPTION_DESC
            }

            SortOrderType.DESCRIPTION_DESC -> {
                ivSortBy.setImageResource(R.drawable.ic_sort_desc_desc)
                currentSortType = nextSortType
                sortNotes()
                nextSortType = SortOrderType.TITLE_ASC
            }
        }
    }

    private fun sortNotes() {
        if (viewFavourites) {
            when (currentSortType) {
                SortOrderType.TITLE_ASC -> {
                    noteController.getAllFavByTitleASC().observe(this) {
                        listByTitle = it
                        updateList()
                    }
                }

                SortOrderType.TITLE_DESC -> {
                    noteController.getAllFavByTitleDESC().observe(this) {
                        listByTitle = it
                        updateList()
                    }
                }

                SortOrderType.DESCRIPTION_ASC -> {
                    noteController.getAllFavByDescASC().observe(this) {
                        listByTitle = it
                        updateList()
                    }
                }

                SortOrderType.DESCRIPTION_DESC -> {
                    noteController.getAllFavByDescDESC().observe(this) {
                        listByTitle = it
                        updateList()
                    }
                }
            }
        } else {
            when (currentSortType) {
                SortOrderType.TITLE_ASC -> {
                    noteController.getAllByTitleASC().observe(this) {
                        listByTitle = it
                        updateList()
                    }
                }

                SortOrderType.TITLE_DESC -> {
                    noteController.getAllByTitleDESC().observe(this) {
                        listByTitle = it
                        updateList()
                    }
                }

                SortOrderType.DESCRIPTION_ASC -> {
                    noteController.getAllByDescASC().observe(this) {
                        listByTitle = it
                        updateList()
                    }
                }

                SortOrderType.DESCRIPTION_DESC -> {
                    noteController.getAllByDescDESC().observe(this) {
                        listByTitle = it
                        updateList()
                    }
                }
            }
        }
    }

    private var animationCounter = 0
    private fun updateList() {
        animationCounter++
        if (animationCounter > 0) {
            adapterNoteList.animate = false
        }
        adapterNoteList.updateList(listByTitle)
    }


    private fun showHideSortOptions(hide: Boolean) {
        if (hide) {
            clSortOptions.visibility = View.GONE
            ivShowHideSortOption.setImageResource(R.drawable.ic_down_arrow)
        } else {
            clSortOptions.visibility = View.VISIBLE
            ivShowHideSortOption.setImageResource(R.drawable.ic_up_arrow)
        }
    }

    private fun deleteSelectedNotes() {
        val job = noteListDelete.map { tblNote ->
            CoroutineScope(Dispatchers.IO).launch {
                noteController.deleteNote(tblNote.pkNoteId!!)
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            job.joinAll()
            Toast.makeText(this@MainActivity, "Deleted", Toast.LENGTH_SHORT).show()

            noteListDelete.clear()
            if (noteListDelete.isEmpty())
                changeMode(ModeMain.ADD)
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
        edtSearchField = findViewById(R.id.edtSearchField)
        ivShowSideDrawer = findViewById(R.id.ivShowSideDrawer)

        clSortOptions = findViewById(R.id.clSortOptions)
        ivShowHideSortOption = findViewById(R.id.ivShowHideSortOption)
        ivSortBy = findViewById(R.id.ivSortBy)
        ivFavourites = findViewById(R.id.ivFavourites)

        llCheckBox = findViewById(R.id.lLCheckBox)
        chkbxSelectAll = findViewById(R.id.chkbxSelectAll_main)

        tvLogin = sideNavigation.getHeaderView(0).findViewById(R.id.tvLogin_sideDrawer)
        sIvProfilePhoto = sideNavigation.getHeaderView(0).findViewById(R.id.sIvProfilePhoto_sideDrawer)
        tvName = sideNavigation.getHeaderView(0).findViewById(R.id.tvName_sideDrawer)
        tvMail = sideNavigation.getHeaderView(0).findViewById(R.id.tvMail_sideDrawer)

        //######### CONTROLLERS #########//
        userController = ViewModelProvider(this)[UserController::class.java]
        noteController = ViewModelProvider(this)[NoteControllers::class.java]

        //######### RECYCLER VIEWS #########//
        rvNoteList = findViewById(R.id.rvNoteList_home)
        rvNoteList.layoutManager = LinearLayoutManager(this)
        adapterNoteList = AdapterNoteList(this, listByTitle, this)
        rvNoteList.adapter = adapterNoteList

        noteController.allNotes.observe(this) { noteList ->
            if (noteList.isEmpty()) {
                llCheckBox.visibility = View.GONE
                chkbxSelectAll.isChecked = false
            }
            adapterNoteList.updateList(noteList)
        }
        setupSwipeToDelete(rvNoteList, adapterNoteList)
    }

    override fun navigateToAddNoteFragment(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val tblNote = getNote(id)

            withContext(Dispatchers.Main) {
                val intent = Intent(baseContext, AddNoteActivity::class.java)
                intent.putExtra(CONST.KEY_TBL_NOTE, tblNote)
                startActivity(intent)
                if (modeMain == ModeMain.DELETE) {
                    modeMain = ModeMain.ADD
                    changeMode(modeMain)
                }
            }
        }
    }

    override fun addToDeleteList(noteList: List<TblNote>) {
        noteListDelete.clear()
        noteListDelete.addAll(noteList)

        if (noteListDelete.isNotEmpty())
            if (modeMain == ModeMain.ADD)
                changeMode(ModeMain.DELETE)
    }

    override fun addToDeleteList(note: TblNote) {
        noteListDelete.add(note)
        if (noteListDelete.isNotEmpty())
            if (modeMain == ModeMain.ADD)
                changeMode(ModeMain.DELETE)
    }

    override fun clearDeleteList() {
        noteListDelete.clear()

        if (noteListDelete.isNotEmpty())
            changeMode(ModeMain.DELETE)
    }

    override fun changeMode(modeMain: ModeMain) {
        this.modeMain = modeMain
        if (modeMain == ModeMain.DELETE) {
            ivAddNote.setImageResource(R.drawable.ic_delete)
            llCheckBox.visibility = View.VISIBLE
        } else if (modeMain == ModeMain.ADD) {
            llCheckBox.visibility = View.GONE
            ivAddNote.setImageResource(R.drawable.ic_add)
        }
    }

    override fun removeFromDeletedList(tblNote: TblNote) {
        noteListDelete.remove(tblNote)
        if (noteListDelete.isEmpty())
            if (modeMain == ModeMain.DELETE)
                changeMode(ModeMain.ADD)
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        loggedIn = false
        tvLogin.text = "Login"
        sIvProfilePhoto.setImageResource(R.drawable.kamake)
        tvName.text = null
        tvMail.text = null
    }

    private fun loadUserData() {
        FirebaseAuth.getInstance().uid.let { uid ->
            if (uid != null) {
                tvLogin.text = "Logout"
                loggedIn = true

                mDBRef.child(CONST.fDB_DIR_USER).child(uid)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = task.result.getValue(TblUser::class.java)

                            if (user != null) {
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
                                Toast.makeText(this, "User loaded...", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Error loading User data: ${task.exception}: ",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {

            }
        }
    }

    private fun getBitmap(drawable: Drawable): Bitmap {
        return (drawable as BitmapDrawable).bitmap
    }

    private fun searchNotes() {
        if (edtSearchField.text.isEmpty()) {
            noteController.allNotes.observe(this) { noteList ->
                adapterNoteList.updateList(noteList)
            }
        } else {
            val searchText = edtSearchField.text.toString()

            CoroutineScope(Dispatchers.IO).launch {
                listByTitle = noteController.searchNotes(searchText)
                withContext(Dispatchers.Main) {
                    adapterNoteList.updateList(listByTitle)
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
            noteController.updateNote(tblNote)
        }
    }

    override fun deleteNote(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            noteController.deleteNote(id)
        }
    }

    override fun updateFavourite(id: Int, favourite: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            noteController.updateFavourite(id, favourite)
        }
    }

    private suspend fun getNote(id: Int): TblNote {
        return withContext(Dispatchers.IO) {
            noteController.getNote(id)
        }
    }

    private fun showHideSideDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
        else
            drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun setStatusBarColor() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = window.decorView
            decorView.systemUiVisibility =
                decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            showHideSideDrawer()
        else {
            if (modeMain == ModeMain.DELETE) {
                modeMain = ModeMain.ADD
                changeMode(ModeMain.ADD)
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
                if (dX == 0.0f) {
                    viewHolder.itemView.setBackgroundResource(R.drawable.bg_list_item)
                } else {
                    val colorObtained = getColorFromValue(this@MainActivity, dX.toInt())
                    viewHolder.itemView.backgroundTintList =
                        ColorStateList.valueOf(colorObtained)
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
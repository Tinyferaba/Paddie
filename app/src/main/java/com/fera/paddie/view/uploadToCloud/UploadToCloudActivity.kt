package com.fera.paddie.view.uploadToCloud

import android.animation.ArgbEvaluator
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fera.paddie.R
import com.fera.paddie.auth.LoginAndSignUp
import com.fera.paddie.controller.NoteControllers
//import com.fera.paddie.controller.NoteControllers
import com.fera.paddie.model.TblNote
import com.fera.paddie.util.CONST
import com.fera.paddie.view.main.addNote.AddNoteActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UploadToCloudActivity : AppCompatActivity(), AdapterUploadNoteList.NoteActivities {
    private val TAG = "UploadToCloudActivity"

    //######### VALUES #########//
    private var mode = MODE.UPLOAD_TO_CLOUD

    //######### VIEWS #########//
    private lateinit var chkbxSelectAll: CheckBox
    private lateinit var ivUploadToCloud: ImageView

    private lateinit var pbUploadStatus: ProgressBar
    private var totalUploaded = 0
    private var totalError = 0
    private var progress = 0

    private var uploadedList = mutableListOf<Int>()
    private var errorList = mutableListOf<Int>()

    //######### NOTEs & TODOs List PROPERTY #########//
    private lateinit var rvNoteListToBeUploaded: RecyclerView
    private lateinit var adapterNoteListToBeUpload: AdapterUploadNoteList

    private lateinit var rvNoteListAlreadyUploaded: RecyclerView
    private lateinit var adapterNoteListAlreadyUploaded: AdapterUploadNoteList

    //######### CONTROLLERS PROPERTY #########//
    private lateinit var noteControllers: NoteControllers

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDBRef: DatabaseReference

    //######### CLOUD #########//
    private var noteListToBeUpload = mutableListOf<TblNote>()
    private var noteToDownload = mutableListOf<TblNote>()
    private var noteListAlreadyUploaded = mutableListOf<TblNote>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_upload_to_cloud)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setMode()
        initViews()
        setUI()

        addActionListeners()
        setStatusBarColor()
        checkSignedInUser()
    }


    private fun addActionListeners() {
        chkbxSelectAll.setOnClickListener {

            val checked = chkbxSelectAll.isChecked
            if (checked) {
//                ivUploadToCloud.visibility = View.VISIBLE
                adapterNoteListToBeUpload.checkAll(true)
            } else {
//                ivUploadToCloud.visibility = View.GONE
                adapterNoteListToBeUpload.checkAll(false)
            }
        }
        ivUploadToCloud.setOnClickListener {
            lifecycleScope.launch {
                if (mode == MODE.UPLOAD_TO_CLOUD) {
                    uploadToCloud()
                } else if (mode == MODE.DOWNLOAD_FROM_CLOUD) {
                    downloadFromCloud()
                }
            }
        }
    }

    private suspend fun downloadFromCloud() {
        val uploadScope = lifecycleScope

        val downloadJob = noteListToBeUpload.map { tblNote ->
            uploadScope.launch {
                noteControllers.insertNote(tblNote)
            }
        }

        downloadJob.forEach { job -> job.join() }
    }

    private fun initViews() {
        mAuth = FirebaseAuth.getInstance()
        mDBRef = FirebaseDatabase.getInstance().getReference()

        chkbxSelectAll = findViewById(R.id.chkbxSelectAll_uploadToCloud)
        ivUploadToCloud = findViewById(R.id.ivUploadToCloud)
        pbUploadStatus = findViewById(R.id.pbUploadStatus)

        //######### CONTROLLERS #########//
        noteControllers = NoteControllers(application)

        //######### RECYCLER VIEWS #########//
        rvNoteListToBeUploaded = findViewById(R.id.rvNoteListNew_toCloud)
        rvNoteListToBeUploaded.layoutManager = LinearLayoutManager(this)
        adapterNoteListToBeUpload = AdapterUploadNoteList(this, noteListToBeUpload, this, false)
        rvNoteListToBeUploaded.adapter = adapterNoteListToBeUpload
        setupSwipeToDelete(rvNoteListToBeUploaded, adapterNoteListToBeUpload)
        if (mode == MODE.UPLOAD_TO_CLOUD) {
            noteControllers.getAllNewNotes.observe(this) { noteList ->
                adapterNoteListToBeUpload.updateNoteList(noteList)
            }
        } else if (mode == MODE.DOWNLOAD_FROM_CLOUD) {
            mDBRef.child(CONST.KEY_TBL_NOTE)
                .child(mAuth.uid!!)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val snapshot = task.result
                        if (snapshot.exists()) {
                            snapshot.children.forEach {
                                val tblNote = it.getValue(TblNote::class.java)
                                noteListToBeUpload.add(tblNote!!)
                            }
                            adapterNoteListToBeUpload.updateNoteList(noteListToBeUpload)
                        } else {
                            Toast.makeText(
                                this,
                                "No Notes for User: ${mAuth.uid}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(this, "Error Fetching Notes...", Toast.LENGTH_SHORT).show()
                    }
                }
        }


        rvNoteListAlreadyUploaded = findViewById(R.id.rvNoteListUploaded_toCloud)
        rvNoteListAlreadyUploaded.layoutManager = LinearLayoutManager(this)
        adapterNoteListAlreadyUploaded =
            AdapterUploadNoteList(this, noteListAlreadyUploaded, this, true)
        rvNoteListAlreadyUploaded.adapter = adapterNoteListAlreadyUploaded
        setupSwipeToDelete(rvNoteListAlreadyUploaded, adapterNoteListAlreadyUploaded)
        noteControllers.getAllUploadedNotes.observe(this) { noteList ->
            adapterNoteListAlreadyUploaded.updateNoteList(noteList)
        }
    }

    private suspend fun uploadToCloud() {
        pbUploadStatus.visibility = View.VISIBLE
        pbUploadStatus.max = noteListToBeUpload.size

        val uploadScope = lifecycleScope

        val uploadJob = noteListToBeUpload.map { tblNote ->
            uploadScope.launch {
                if (tblNote.key == null) {
                    val key = mDBRef.child(CONST.KEY_TBL_NOTE).push().key

                    tblNote.key = key
                    tblNote.updated = false

                    noteControllers.updateNote(tblNote)

                    mDBRef.child(CONST.KEY_TBL_NOTE).child(mAuth.uid!!).child(key!!)
                        .setValue(tblNote)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                uploadedList.add(tblNote.pkNoteId!!)
                                totalUploaded++
                                progress++

                                pbUploadStatus.progress = progress
                            } else {
                                errorList.add(tblNote.pkNoteId!!)
                                totalError++

                                Toast.makeText(
                                    applicationContext,
                                    "Problem: ${task.exception}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else if (tblNote.updated) {
                    val key = tblNote.key

                    tblNote.key = key
                    tblNote.updated = false

                    noteControllers.updateNote(tblNote)

                    val updated = mapOf<String, Any>(
                        "title" to tblNote.title.toString(),
                        "description" to tblNote.description.toString(),
                        "favourite" to tblNote.favourite,
                        "dateModified" to tblNote.dateModified
                    )

                    mDBRef.child(CONST.KEY_TBL_NOTE).child(key!!)
                        .updateChildren(updated)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                uploadedList.add(tblNote.pkNoteId!!)
                                totalUploaded++
                                progress++

                                pbUploadStatus.progress = progress
                            } else {
                                errorList.add(tblNote.pkNoteId!!)
                                totalError++

                                Toast.makeText(
                                    applicationContext,
                                    "Problem: ${task.exception}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }
        }

        uploadJob.forEach { job -> job.join() }

        pbUploadStatus.visibility = View.GONE
        withContext(Dispatchers.Main) {
            Toast.makeText(
                applicationContext,
                "Uploaded: $totalUploaded \nFailed: $totalError",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun navigateToAddNoteFragment(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val tblNote = getNote(id)

            withContext(Dispatchers.Main) {
                val intent = Intent(baseContext, AddNoteActivity::class.java)
                intent.putExtra(CONST.KEY_TBL_NOTE, tblNote)
                startActivity(intent)
            }
        }
    }

    fun addToUploadList(tblNote: TblNote) {
        if (mode == MODE.UPLOAD_TO_CLOUD){
            noteListToBeUpload.add(tblNote)
            hideViewsOnEmptyList(noteListToBeUpload.isEmpty())
        } else if (mode == MODE.DOWNLOAD_FROM_CLOUD){
            noteToDownload.add(tblNote)
            hideViewsOnEmptyList(noteToDownload.isEmpty())
        }
    }

    fun addToUploadList(list: List<TblNote>) {
        if (mode == MODE.UPLOAD_TO_CLOUD){
            noteListToBeUpload.clear()
            noteListToBeUpload.addAll(list)
            hideViewsOnEmptyList(noteListToBeUpload.isEmpty())
        } else if (mode == MODE.DOWNLOAD_FROM_CLOUD){
            noteToDownload.clear()
            noteToDownload.addAll(list)
            hideViewsOnEmptyList(noteToDownload.isEmpty())
        }
        Log.d(TAG, "addToUploadList: $list \n$noteListToBeUpload")
    }

    fun removeFromUploadList(tblNote: TblNote) {
        if (mode == MODE.UPLOAD_TO_CLOUD){
            noteListToBeUpload.remove(tblNote)
            hideViewsOnEmptyList(noteListToBeUpload.isEmpty())
        } else if (mode == MODE.DOWNLOAD_FROM_CLOUD){
            noteToDownload.remove(tblNote)
            hideViewsOnEmptyList(noteToDownload.isEmpty())
        }
    }

    private fun hideViewsOnEmptyList(listIsEmpty: Boolean) {
        if (listIsEmpty) {
            ivUploadToCloud.visibility = View.GONE
        } else {
            ivUploadToCloud.visibility = View.VISIBLE
        }
    }

    private fun setStatusBarColor() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = window.decorView
            decorView.systemUiVisibility =
                decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }


    override fun clearUploadList() {
        if (mode == MODE.UPLOAD_TO_CLOUD){
            noteListToBeUpload.clear()
        }
        hideViewsOnEmptyList(noteListToBeUpload.isEmpty())
        Log.d(TAG, "addToUploadList: \n$noteListToBeUpload")
    }

    override suspend fun getNote(id: Int): TblNote {
        return withContext(Dispatchers.IO) {
            noteControllers.getNote(id)
        }
    }

    override fun updateNote(tblNote: TblNote) {
        CoroutineScope(Dispatchers.IO).launch {
            noteControllers.updateNote(tblNote)
        }
    }

    override fun deleteNote(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            noteControllers.deleteNote(id)
        }
    }

    override fun updateFavourite(id: Int, isFavourite: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            noteControllers.updateFavourite(id, isFavourite)
        }
    }

    private fun setMode() {
        val uploadToCloud = intent.getBooleanExtra("uploadToCloud", true)
        if (uploadToCloud)
            mode = MODE.UPLOAD_TO_CLOUD
        else
            mode = MODE.DOWNLOAD_FROM_CLOUD
    }

    private fun setUI() {
        if (mode == MODE.UPLOAD_TO_CLOUD) {
            findViewById<TextView>(R.id.activityTitle).setText("Upload To Cloud")
            findViewById<TextView>(R.id.titleDescription).setText("Not Yet Uploaded")
            findViewById<TextView>(R.id.titleDescription2).setText("Already Uploaded")
            ivUploadToCloud.setImageResource(R.drawable.ic_upload)
        } else if (mode == MODE.DOWNLOAD_FROM_CLOUD) {
            findViewById<TextView>(R.id.activityTitle).setText("Download From Cloud")
            findViewById<TextView>(R.id.titleDescription).setText("Not Yet Downloaded")
            findViewById<TextView>(R.id.titleDescription2).setText("Already Downloaded")
            ivUploadToCloud.setImageResource(R.drawable.ic_download)
        }
    }

    private fun checkSignedInUser() {
        if (mAuth.currentUser == null) {
            val alert = AlertDialog.Builder(this)
            alert.setMessage("No Account\nPlease login to backup your notes.")
                .setCancelable(false)
                .setTitle("No Account")
                .setPositiveButton("Login", DialogInterface.OnClickListener { dialog, which ->
                    val intent = Intent(this, LoginAndSignUp::class.java)
                    startActivity(intent)
                    finish()
                })
                .setNeutralButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                    onBackPressedDispatcher.onBackPressed()
                    finish()
                }).show()
        }
    }

    private fun setupSwipeToDelete(recyclerView: RecyclerView, adapter: AdapterUploadNoteList) {
        val itemTouchHelperCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                // onMove is for drag & drop, which we don't need here
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
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
                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    // Customize the swipe background and icon (optional)
                    if (dX == 0.0f) {
                        viewHolder.itemView.setBackgroundResource(R.drawable.bg_list_item)
                    } else {
                        val colorObtained =
                            getColorFromValue(this@UploadToCloudActivity, dX.toInt())
                        viewHolder.itemView.backgroundTintList =
                            ColorStateList.valueOf(colorObtained)
                    }
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
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
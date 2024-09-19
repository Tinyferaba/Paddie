package com.fera.paddie.view.uploadToCloud

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fera.paddie.R
import com.fera.paddie.auth.LoginAndSignUp
import com.fera.paddie.controller.NoteControllers
//import com.fera.paddie.controller.NoteControllers
import com.fera.paddie.model.TblNote
import com.fera.paddie.model.util.CONST
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
    private lateinit var rvNoteListNew: RecyclerView
    private lateinit var adapterNoteListNew: AdapterUploadNoteList

    private lateinit var rvNoteListOld: RecyclerView
    private lateinit var adapterNoteListOld: AdapterUploadNoteList

    //######### CONTROLLERS PROPERTY #########//
    private lateinit var noteControllers: NoteControllers

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDBRef: DatabaseReference

    //######### CLOUD #########//
    private var noteListNew = mutableListOf<TblNote>()
    private var noteListOld = mutableListOf<TblNote>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_upload_to_cloud)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        addActionListeners()
        setStatusBarColor()
        checkSignedInUser()
    }

    private fun checkSignedInUser() {
        if (mAuth.currentUser == null){
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

    private fun addActionListeners() {
        chkbxSelectAll.setOnClickListener {
            val checked = chkbxSelectAll.isChecked
            noteListNew.clear()

            if (checked) {
//                ivUploadToCloud.visibility = View.VISIBLE
                adapterNoteListNew.checkAll(true)
            } else {
//                ivUploadToCloud.visibility = View.GONE
                adapterNoteListNew.checkAll(false)
            }
        }
        ivUploadToCloud.setOnClickListener {
            lifecycleScope.launch {
                uploadToCloud()
            }
        }
    }

    private fun initViews() {
        mAuth = FirebaseAuth.getInstance()
        mDBRef = FirebaseDatabase.getInstance().getReference()

        chkbxSelectAll = findViewById(R.id.chkbxSelectAll)
        ivUploadToCloud = findViewById(R.id.ivUploadToCloud)
        pbUploadStatus = findViewById(R.id.pbUploadStatus)

        //######### CONTROLLERS #########//
        noteControllers = NoteControllers(application)

        //######### RECYCLER VIEWS #########//
        rvNoteListNew = findViewById(R.id.rvNoteListNew_toCloud)
        rvNoteListNew.layoutManager = LinearLayoutManager(this)
        adapterNoteListNew = AdapterUploadNoteList(this, noteListNew, this)
        rvNoteListNew.adapter = adapterNoteListNew
        noteControllers.getAllNewNotes.observe(this) { noteList ->
            adapterNoteListNew.updateNoteList(noteList)
        }

        rvNoteListOld = findViewById(R.id.rvNoteListUploaded_toCloud)
        rvNoteListOld.layoutManager = LinearLayoutManager(this)
        adapterNoteListOld = AdapterUploadNoteList(this, noteListOld, this)
        rvNoteListOld.adapter = adapterNoteListOld
        noteControllers.getAllUploadedNotes.observe(this){ noteList ->
            adapterNoteListOld.updateNoteList(noteList)
        }

    }

    private suspend fun uploadToCloud() {
        pbUploadStatus.visibility = View.VISIBLE
        pbUploadStatus.max = noteListNew.size

        val uploadScope = lifecycleScope

        val uploadJob = noteListNew.map { tblNote ->
            uploadScope.launch {
                if (tblNote.key == null) {
                    val key = mDBRef.child(CONST.KEY_TBL_NOTE).push().key

                    tblNote.key = key
                    tblNote.updated = false

                    noteControllers.updateNote(tblNote)

                    mDBRef.child(CONST.KEY_TBL_NOTE).child(key!!)
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

                                Toast.makeText(applicationContext, "Problem: ${task.exception}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else if (tblNote.updated){
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

                                Toast.makeText(applicationContext, "Problem: ${task.exception}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }

        uploadJob.forEach { job -> job.join()}

        pbUploadStatus.visibility = View.GONE
        withContext(Dispatchers.Main){
            Toast.makeText(applicationContext, "Uploaded: $totalUploaded \nFailed: $totalError", Toast.LENGTH_LONG).show()
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

    override fun clearUploadList() {
        noteListNew.clear()
        hideViewsOnEmptyList(noteListNew.isEmpty())
    }

    override suspend fun getNote(id: Int): TblNote {
        return withContext(Dispatchers.IO) {
            noteControllers.getNote(id)
        }
    }

    fun addToUploadList(tblNote: TblNote) {
        noteListNew.add(tblNote)
        hideViewsOnEmptyList(noteListNew.isEmpty())
    }

    fun addToUploadList(list: List<TblNote>) {
        noteListNew.clear()
        noteListNew.addAll(list)
        hideViewsOnEmptyList(noteListNew.isEmpty())
    }

    fun removeFromUploadList(tblNote: TblNote) {
        noteListNew.remove(tblNote)
        hideViewsOnEmptyList(noteListNew.isEmpty())
    }

    private fun hideViewsOnEmptyList(listIsEmpty: Boolean){
        if (listIsEmpty){
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
}
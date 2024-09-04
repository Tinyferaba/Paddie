package com.fera.paddie.view.uploadToCloud

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fera.paddie.R
import com.fera.paddie.controller.NoteControllers
//import com.fera.paddie.controller.NoteControllers
import com.fera.paddie.model.TblNote
import com.fera.paddie.model.util.CONST
import com.fera.paddie.view.main.addNote.AddNoteActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    private lateinit var mDBRef: DatabaseReference

    //######### CLOUD #########//
    private var noteList = mutableListOf<TblNote>()

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
    }

    private fun addActionListeners() {
        chkbxSelectAll.setOnClickListener {
            val checked = chkbxSelectAll.isChecked
            noteList.clear()

            if (checked) {
                ivUploadToCloud.visibility = View.VISIBLE
                adapterNoteListNew.checkAll(true)
            } else {
                ivUploadToCloud.visibility = View.GONE
                adapterNoteListNew.checkAll(false)
            }
        }
        ivUploadToCloud.setOnClickListener {
            //TODO: Upload to Cloud
            Log.d(TAG, "addActionListeners: $noteList")
            Toast.makeText(this, "Uploading to CLOUD", Toast.LENGTH_SHORT).show()
            lifecycleScope.launch {
                uploadToCloud()
            }
        }
    }

    private fun initViews() {
        mDBRef = FirebaseDatabase.getInstance().getReference()

        chkbxSelectAll = findViewById(R.id.chkbxSelectAll)
        ivUploadToCloud = findViewById(R.id.ivUploadToCloud)
        pbUploadStatus = findViewById(R.id.pbUploadStatus)

        //######### CONTROLLERS #########//
        noteControllers = NoteControllers(application)

        //######### RECYCLER VIEWS #########//
        rvNoteListNew = findViewById(R.id.rvNoteListNew_toCloud)
        rvNoteListNew.layoutManager = LinearLayoutManager(this)
        noteControllers.getAllNewNotes.observe(this) { noteList ->
            adapterNoteListNew = AdapterUploadNoteList(this, noteList, this)
            rvNoteListNew.adapter = adapterNoteListNew
        }

        rvNoteListOld = findViewById(R.id.rvNoteListUploaded_toCloud)
        rvNoteListOld.layoutManager = LinearLayoutManager(this)
        noteControllers.getAllUploadedNotes.observe(this){ noteList ->
            adapterNoteListOld = AdapterUploadNoteList(this, noteList, this)
            rvNoteListOld.adapter = adapterNoteListOld
        }
    }

    private suspend fun uploadToCloud() {
        pbUploadStatus.visibility = View.VISIBLE
        pbUploadStatus.max = noteList.size

        noteList.forEach { tblNote ->
            if (tblNote.key == null) {
                val key = mDBRef.child(CONST.KEY_TBL_NOTE).push().key
                tblNote.key = key

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

                            Toast.makeText(this, "Problem: ${task.exception}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else if (tblNote.updated){
                val key = tblNote.key

                tblNote.key = key

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

                            Toast.makeText(this, "Problem: ${task.exception}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        delay(3000)
        pbUploadStatus.visibility = View.GONE
        Toast.makeText(this, "Uploaded: $totalUploaded \nError: $totalError", Toast.LENGTH_SHORT).show()
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

    override suspend fun getNote(id: Int): TblNote {
        return withContext(Dispatchers.IO) {
            noteControllers.getNote(id)
        }
    }

    fun addToUploadList(tblNote: TblNote) {
        noteList.add(tblNote)
    }

    fun addToUploadList(list: List<TblNote>) {
        noteList.addAll(list)
    }

    fun removeFromUploadList(tblNote: TblNote) {
        noteList.remove(tblNote)
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
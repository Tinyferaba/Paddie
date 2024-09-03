package com.fera.paddie.view.uploadToCloud

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fera.paddie.R
//import com.fera.paddie.controller.NoteControllers
import com.fera.paddie.model.TblNote
import com.fera.paddie.model.util.CONST
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UploadToCloudActivity : AppCompatActivity(),AdapterUploadNoteList.NoteActivities {
    private val TAG = "UploadToCloudActivity"

    private lateinit var chkbxSelectAll: CheckBox
    private lateinit var ivUploadToCloud: ImageView

    //######### NOTEs & TODOs List PROPERTY #########//
    private lateinit var rvNoteList: RecyclerView
    private lateinit var adapterNoteList: AdapterUploadNoteList

    //######### CONTROLLERS PROPERTY #########//
//    private lateinit var noteControllers: NoteControllers

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
        chkbxSelectAll.setOnClickListener{
            val checked = chkbxSelectAll.isChecked
            noteList.clear()

            if (checked){
                adapterNoteList.checkAll(true)
            } else {
                adapterNoteList.checkAll(false)
            }
        }
        ivUploadToCloud.setOnClickListener {
            //TODO: Upload to Cloud
            Log.d(TAG, "addActionListeners: $noteList")
            Toast.makeText(this, "Uploading to CLOUD", Toast.LENGTH_SHORT).show()
            uploadToCloud()
        }
    }

    private fun uploadToCloud() {
        noteList.forEach { tblNote ->
            val key = mDBRef.child(CONST.KEY_TBL_NOTE).push().key
            mDBRef.child(CONST.KEY_TBL_NOTE).child(key!!)
                .setValue(tblNote)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        //TODO: Show status in progress bar...
                    } else {
                        Toast.makeText(this, "Problem: ${task.exception}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun initViews() {
        mDBRef = FirebaseDatabase.getInstance().getReference()

        chkbxSelectAll = findViewById(R.id.chkbxSelectAll)
        ivUploadToCloud = findViewById(R.id.ivUploadToCloud)

        //######### CONTROLLERS #########//
//        noteControllers = NoteControllers(application)

        //######### RECYCLER VIEWS #########//
        rvNoteList = findViewById(R.id.rvNoteList_toCloud)
        rvNoteList.layoutManager = LinearLayoutManager(this)
//        noteControllers.allNotes.observe(this){noteList ->
//            adapterNoteList = AdapterUploadNoteList(this, noteList, this)
//            rvNoteList.adapter = adapterNoteList
//        }
    }

    override fun updateNote(tblNote: TblNote) {
//        CoroutineScope(Dispatchers.IO).launch {
//            noteControllers.updateNote(tblNote)
//        }
    }

    override fun deleteNote(id: String) {
//        CoroutineScope(Dispatchers.IO).launch {
//            noteControllers.deleteNote(id)
//        }
    }

    override fun updateFavourite(id: String, isFavourite: Boolean) {
//        CoroutineScope(Dispatchers.IO).launch {
//            noteControllers.updateFavourite(id, isFavourite)
//        }
    }

    override suspend fun getNote(id: String): TblNote {
//        return withContext(Dispatchers.IO){
//            noteControllers.getNote(id)
//        }
        return TblNote()
    }

    fun addToUploadList(tblNote: TblNote){
        noteList.add(tblNote)
    }

    fun addToUploadList(list: List<TblNote>){
        noteList.addAll(list)
    }

    fun removeFromUploadList(tblNote: TblNote){
        noteList.remove(tblNote)
    }

    private fun setStatusBarColor(){
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = window.decorView
            decorView.systemUiVisibility = decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }
}
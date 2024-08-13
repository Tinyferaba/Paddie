package com.fera.paddie.view.uploadToCloud

import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fera.paddie.R
import com.fera.paddie.controller.NoteControllers
import com.fera.paddie.model.TblNote
import com.fera.paddie.view.main.home.AdapterNoteList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UploadToCloudActivity : AppCompatActivity(),AdapterUploadNoteList.NoteActivities {
    private val TAG = "UploadToCloudActivity"

    private lateinit var chkbxSelectAll: CheckBox
    private lateinit var ivUploadToCloud: ImageView

    //######### NOTEs & TODOs List PROPERTY #########//
    private lateinit var rvNoteList: RecyclerView
    private lateinit var adapterNoteList: AdapterUploadNoteList

    //######### CONTROLLERS PROPERTY #########//
    private lateinit var noteControllers: NoteControllers

    //######### CLOUD #########//
    private var uploadList = mutableListOf<TblNote>()

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
            uploadList.clear()

            if (checked){
                adapterNoteList.checkAll(true)
            } else {
                adapterNoteList.checkAll(false)
            }
        }
        ivUploadToCloud.setOnClickListener {
            //TODO: Upload to Cloud
            Log.d(TAG, "addActionListeners: $uploadList")
            Toast.makeText(this, "Uploading to CLOUD", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        chkbxSelectAll = findViewById(R.id.chkbxSelectAll)
        ivUploadToCloud = findViewById(R.id.ivUploadToCloud)

        //######### CONTROLLERS #########//
        noteControllers = NoteControllers(application)

        //######### RECYCLER VIEWS #########//
        rvNoteList = findViewById(R.id.rvNoteList_toCloud)
        rvNoteList.layoutManager = LinearLayoutManager(this)
        noteControllers.allNotes.observe(this){noteList ->
            adapterNoteList = AdapterUploadNoteList(this, noteList, this)
            rvNoteList.adapter = adapterNoteList
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

    override suspend fun getNote(id: Int): TblNote {
        return withContext(Dispatchers.IO){
            noteControllers.getNote(id)
        }
    }

    fun addToUploadList(tblNote: TblNote){
        uploadList.add(tblNote)
    }

    fun addToUploadList(list: List<TblNote>){
        uploadList.addAll(list)
    }

    fun removeFromUploadList(tblNote: TblNote){
        uploadList.remove(tblNote)
    }

    private fun setStatusBarColor(){
        window.statusBarColor = ContextCompat.getColor(this, R.color.gray)
    }
}
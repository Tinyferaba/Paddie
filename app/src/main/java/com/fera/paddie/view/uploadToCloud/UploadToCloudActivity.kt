package com.fera.paddie.view.uploadToCloud

import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fera.paddie.R
import com.fera.paddie.controller.NoteControllers
import com.fera.paddie.model.TblNote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UploadToCloudActivity : AppCompatActivity(),AdapterUploadNoteList.NoteActivities {
    private val TAG = "UploadToCloudActivity"

    private lateinit var chkbxSelectAll: CheckBox
    private lateinit var ivUploadToCloud: ImageView

    //######### CONTROLLERS PROPERTY #########//
    private lateinit var noteControllers: NoteControllers

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

    }

    private fun addActionListeners() {
        chkbxSelectAll.setOnClickListener{
            //TODO: Check/Uncheck all for upload to cloud
        }
        ivUploadToCloud.setOnClickListener {
            //TODO: Upload to Cloud
            Toast.makeText(this, "Uploading to CLOUD", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        chkbxSelectAll = findViewById(R.id.chkbxSelectAll)
        ivUploadToCloud = findViewById(R.id.ivUploadToCloud)

        //######### CONTROLLERS #########//
        noteControllers = NoteControllers(application)
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
}
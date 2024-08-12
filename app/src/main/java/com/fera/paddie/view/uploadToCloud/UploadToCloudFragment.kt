package com.fera.paddie.view.uploadToCloud

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Toast
import com.fera.paddie.R
import com.fera.paddie.controller.NoteControllers
import com.fera.paddie.model.TblNote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class UploadToCloudFragment : Fragment(),AdapterUploadNoteList.NoteActivities {

    private lateinit var v: View
    private lateinit var chkbxSelectBox: CheckBox
    private lateinit var ivUploadToCloud: ImageView

    //######### CONTROLLERS PROPERTY #########//
    private lateinit var noteControllers: NoteControllers
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_upload_to_cloud, container, false)

        initViews()
        addActionListeners()

        return v
    }

    private fun addActionListeners() {
        chkbxSelectBox.setOnClickListener{
            //TODO: Check/Uncheck all for upload to cloud
        }
        ivUploadToCloud.setOnClickListener {
            //TODO: Upload to Cloud
            Toast.makeText(requireContext(), "Uploading to CLOUD", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        chkbxSelectBox = v.findViewById(R.id.chkbxSelectBox)
        ivUploadToCloud = v.findViewById(R.id.ivUploadToCloud)
        //######### CONTROLLERS #########//
        noteControllers = NoteControllers(requireActivity().application)
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
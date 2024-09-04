package com.fera.paddie.view.main.addNote

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.fera.paddie.R
import com.fera.paddie.controller.NoteControllers
import com.fera.paddie.model.TblNote
import com.fera.paddie.model.util.CONST
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class AddNoteActivity : AppCompatActivity() {

    //######### STATE & VALUES #########//
    private var changesMade = false
    private var favourite = false
    private lateinit var tblNote: TblNote

    private lateinit var v: View
    private lateinit var ivBack: ImageView      //Image View
    private lateinit var ivSave: ImageView
    private lateinit var ivEdit: ImageView
    private lateinit var edtTitle: EditText     //Edit Text
    private lateinit var edtDesc: EditText
    private lateinit var ivFavourite: ImageView

    private lateinit var noteControllers: NoteControllers
    private lateinit var mDBRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_note)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        addActionListeners()
        loadNote()
    }

    private fun addActionListeners() {

        ivBack.setOnClickListener {
            if (changesMade) {
                saveNote()
                changesMade = false
                toggleEditability(false)
            }
            onBackPressed()
        }
        ivSave.setOnClickListener {
            if (tblNote.pkNoteId == null) {
                saveNote()
            } else {
                updateNote()
            }
            changesMade = false
            toggleEditability(false)
        }
        ivEdit.setOnClickListener {
            toggleEditability(true)
        }
        ivFavourite.setOnClickListener {
            favourite = !favourite

            if (favourite) {
                ivFavourite.setImageResource(R.drawable.ic_favourite)
            } else {
                ivFavourite.setImageResource(R.drawable.ic_unfavourite)
            }

            tblNote.favourite = favourite
            updateFavourite(tblNote.pkNoteId!!, favourite)
        }

        //######### LISTEN TO CHANGES MADE #########//
        edtTitle.addTextChangedListener { changesMade() }
        edtDesc.addTextChangedListener { changesMade() }
    }

    private fun initViews() {

        mDBRef = FirebaseDatabase.getInstance().getReference()
        tblNote = TblNote()
        noteControllers = NoteControllers(application)

        //######### VIEWS #########//
        ivBack = v.findViewById(R.id.ivBackAddNoteTodo)
        ivSave = v.findViewById(R.id.ivSaveNoteTodo)
        ivEdit = v.findViewById(R.id.ivEditNote)
        edtTitle = v.findViewById(R.id.edtTitleNoteTodo)
        edtDesc = v.findViewById(R.id.edtDescNoteTodo)
        ivFavourite = v.findViewById(R.id.ivFavourite_addNote)
    }


    private fun updateNote() {
        CoroutineScope(Dispatchers.IO).launch {
            tblNote.title = edtTitle.text.toString()
            tblNote.description = edtDesc.text.toString()
            tblNote.favourite = favourite
            tblNote.dateModified = Date().time

            noteControllers.updateNote(tblNote)

        }
    }

    private fun loadNote() {
        val tmpNote = intent.getParcelableExtra<TblNote>(CONST.KEY_TBL_NOTE)

        if (tmpNote != null)
            setData(tmpNote)
    }

    private fun setData(tmpNote: TblNote) {
        tblNote = tmpNote

        edtTitle.setText(tmpNote.title)
        edtDesc.setText(tmpNote.description)

        if (tmpNote.favourite) {
            favourite = true
            ivFavourite.setImageResource(R.drawable.ic_favourite)
        } else {
            favourite = false
            ivFavourite.setImageResource(R.drawable.ic_unfavourite)
        }

        toggleEditability(true)
    }


    private fun toggleEditability(editMode: Boolean) {
        if (editMode) {
            ivSave.visibility = View.VISIBLE
            ivEdit.visibility = View.GONE
            edtTitle.isEnabled = true
            edtDesc.isEnabled = true
        } else {
            ivSave.visibility = View.GONE
            ivEdit.visibility = View.VISIBLE
            edtTitle.isEnabled = false
            edtDesc.isEnabled = false
        }
    }


    private fun saveNote() {
        CoroutineScope(Dispatchers.IO).launch {
            tblNote.title = edtTitle.text.toString()
            tblNote.description = edtDesc.text.toString()
            tblNote.favourite = favourite
            tblNote.dateCreated = Date().time
            tblNote.dateModified = Date().time

            val noteKey = mDBRef.child(CONST.KEY_TBL_NOTE).push().key
            tblNote.key = noteKey!!


            noteControllers.insertNote(tblNote)
        }

    }

    private fun changesMade() {
        changesMade = true
        ivSave.visibility = View.VISIBLE
    }

    private fun updateFavourite(id: Int, favourite: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            noteControllers.updateFavourite(id, favourite)
        }
    }
}
package com.fera.paddie.view.main.addNote

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.fera.paddie.R
import com.fera.paddie.controller.NoteControllers
import com.fera.paddie.model.TblNote
import com.fera.paddie.model.util.CONST
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class AddNoteActivity : AppCompatActivity() {
    private val TAG = "AddNoteActivity"

    //######### STATE & VALUES #########//
    private var changesMade = false
    private var favourite = false
    private lateinit var tblNote: TblNote

    private lateinit var ivBack: ImageView      //Image View
    private lateinit var ivSave: ImageView
    private lateinit var ivEdit: ImageView
    private lateinit var edtTitle: EditText     //Edit Text
    private lateinit var edtDesc: EditText
    private lateinit var ivFavourite: ImageView

    private lateinit var noteControllers: NoteControllers

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

        setStatusBarColor()
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
            if (tblNote.pkNoteId != null)
                updateFavourite(tblNote.pkNoteId!!, favourite)
        }

        //######### LISTEN TO CHANGES MADE #########//
        edtTitle.addTextChangedListener { changesMade() }
        edtDesc.addTextChangedListener { changesMade() }
    }

    private fun initViews() {
        tblNote = TblNote()
        noteControllers = NoteControllers(application)

        //######### VIEWS #########//
        ivBack = findViewById(R.id.ivBackAddNoteTodo)
        ivSave = findViewById(R.id.ivSaveNoteTodo)
        ivEdit = findViewById(R.id.ivEditNote)
        edtTitle = findViewById(R.id.edtTitleNote)
        edtDesc = findViewById(R.id.edtDescNote)
        ivFavourite = findViewById(R.id.ivFavourite_addNote)
    }


    private fun updateNote() {
        if (validateNote()) {
            CoroutineScope(Dispatchers.IO).launch {
                tblNote.title = edtTitle.text.toString()
                tblNote.description = edtDesc.text.toString()
                tblNote.favourite = favourite
                tblNote.dateModified = Date().time

                noteControllers.updateNote(tblNote)
            }
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
        if (validateNote()) {
            CoroutineScope(Dispatchers.IO).launch {
                tblNote.title = edtTitle.text.toString()
                tblNote.description = edtDesc.text.toString()
                tblNote.favourite = favourite
                tblNote.dateCreated = Date().time
                tblNote.dateModified = Date().time

                val pkNoteId = noteControllers.insertNote(tblNote)
                tblNote.pkNoteId = pkNoteId

                withContext(Dispatchers.Main) {
                    Log.d(TAG, "saveNote: $tblNote")
                }
            }
        }
    }

    private fun validateNote(): Boolean {
        val title = edtTitle.text.isNotEmpty()
        val desc = edtDesc.text.isNotEmpty()

        return (title || desc)
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

    override fun onBackPressed() {
        super.onBackPressed()
        if (changesMade)
            saveNote()
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
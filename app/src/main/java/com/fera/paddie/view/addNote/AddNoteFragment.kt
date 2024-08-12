package com.fera.paddie.view.addNote

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.fera.paddie.R
import com.fera.paddie.controller.NoteControllers
import com.fera.paddie.model.TblNote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class AddNoteFragment : Fragment() {
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
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_add_note, container, false)

        initViews()
        addActionListeners()
        loadNote()

        return v
    }

    private fun addActionListeners() {
        ivBack.setOnClickListener {
            if (changesMade) {
                saveNote()
                changesMade = false
                toggleEditability(false)
            }
            goBack()
        }
        ivSave.setOnClickListener {
            if (tblNote.pkNoteTodoId == -1) {
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

            tblNote.isFavourite = favourite
            updateFavourite(tblNote.pkNoteTodoId, favourite)
        }

        //######### LISTEN TO CHANGES MADE #########//
        edtTitle.addTextChangedListener { changesMade() }
        edtDesc.addTextChangedListener { changesMade() }
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

    private fun initViews() {
        tblNote = TblNote(-1, false, "", "", Date(), Date())
        noteControllers = NoteControllers(requireActivity().application)

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
            tblNote.isFavourite = favourite
            tblNote.dateModified = Date()

            noteControllers.updateNote(tblNote)
        }

        Toast.makeText(requireContext(), "Updating Note", Toast.LENGTH_SHORT).show()
    }

    private fun loadNote() {
        val tmpNote: TblNote? = arguments?.getParcelable("tblNote")

        if (tmpNote != null)
            setData(tmpNote)
    }

    private fun setData(tmpNote: TblNote) {
        edtTitle.setText(tmpNote.title)
        edtDesc.setText(tmpNote.description)

        if (tmpNote.isFavourite) {
            favourite = true
            ivFavourite.setImageResource(R.drawable.ic_favourite)
        } else {
            favourite = false
            ivFavourite.setImageResource(R.drawable.ic_unfavourite)
        }

        toggleEditability(true)
    }


    private fun saveNote() {
        CoroutineScope(Dispatchers.IO).launch {
            tblNote.title = edtTitle.text.toString()
            tblNote.description = edtDesc.text.toString()
            tblNote.isFavourite = favourite
            tblNote.dateCreated = Date()
            tblNote.dateModified = Date()

            noteControllers.insertNote(tblNote)
        }
        Toast.makeText(requireContext(), "Saving Note", Toast.LENGTH_SHORT).show() //Change it to Note saved if returned value is != -1
    }

    private fun changesMade() {
        changesMade = true
        ivSave.visibility = View.VISIBLE
    }

    private fun goBack() {
        parentFragmentManager.popBackStack()
    }

    private fun updateFavourite(id: Int, favourite: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            noteControllers.updateFavourite(id, favourite)
        }
    }
}
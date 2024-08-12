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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddNoteFragment : Fragment() {
    //######### STATE & VALUES #########//
    private var changesMade = false

    private lateinit var v: View
    private lateinit var ivBack: ImageView      //Image View
    private lateinit var ivSave: ImageView
    private lateinit var edtTitle: EditText     //Edit Text
    private lateinit var edtDesc: EditText

    private lateinit var noteControllers: NoteControllers
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_add_note, container, false)

        initViews()
        addActionListeners()

        return v
    }

    private fun addActionListeners() {
        ivBack.setOnClickListener { goBack() }
        ivSave.setOnClickListener {
            saveNote()
        }

        //######### LISTEN TO CHANGES MADE #########//
        edtTitle.addTextChangedListener { changesMade() }
        edtDesc.addTextChangedListener { changesMade() }
    }

    private fun initViews() {
        noteControllers = NoteControllers(requireActivity().application)

        //######### VIEWS #########//
        ivBack = v.findViewById(R.id.ivBackAddNoteTodo)
        ivSave = v.findViewById(R.id.ivSaveNoteTodo)
        edtTitle = v.findViewById(R.id.edtTitleNoteTodo)
        edtDesc = v.findViewById(R.id.edtDescNoteTodo)

    }

    private fun clearFields() {
        //todo: Complete this and add it in onDestroy method or in restoreProperties methods
        //######### NOTES #########//
        edtTitle.setText("")
        edtDesc.setText("")
    }

    private fun saveNote() {
        CoroutineScope(Dispatchers.IO).launch {
            //TODO
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
}
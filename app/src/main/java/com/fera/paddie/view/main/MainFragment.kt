package com.fera.paddie.view.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fera.paddie.R
import com.fera.paddie.controller.NoteControllers
import com.fera.paddie.model.TblNote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainFragment : Fragment(), AdapterNoteList.NoteActivities {

    private lateinit var v: View
    private lateinit var ivSearch: ImageView
    private lateinit var ivClearSearchField: ImageView
    private lateinit var ivAddNote: ImageView //Buttons
    private lateinit var edtSearchField: EditText

    //######### NOTEs & TODOs List PROPERTY #########//
    private lateinit var rvNoteTodoList: RecyclerView
    private lateinit var adapterNoteList: AdapterNoteList

    //######### CONTROLLERS PROPERTY #########//
    private lateinit var noteControllers: NoteControllers
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_main, container, false)

        initViews()
        addActionListeners()

        return v
    }

    private fun addActionListeners() {
        ivAddNote.setOnClickListener {
            findNavController().navigate(R.id.addNoteFragment)
        }

        ivClearSearchField.setOnClickListener { clearSearchField() }
        ivSearch.setOnClickListener { searchNotes() }
        edtSearchField.addTextChangedListener { searchNotes() }
    }

    private fun initViews() {
        //######### VIEWS #########//
        ivSearch = v.findViewById(R.id.ivSearchNoteAndTodo)     //Image Views
        ivClearSearchField = v.findViewById(R.id.ivClearSearchField)
        ivAddNote = v.findViewById(R.id.ivAddNote)
        edtSearchField = v.findViewById(R.id.edtSearchField)

        //######### CONTROLLERS #########//
        noteControllers = NoteControllers(requireActivity().application)

        //######### RECYCLER VIEWS #########//
        rvNoteTodoList = v.findViewById(R.id.rvNoteList)
        rvNoteTodoList.layoutManager = LinearLayoutManager(requireContext())
        noteControllers.allNotes.observe(viewLifecycleOwner) {noteList ->
            adapterNoteList = AdapterNoteList(requireContext(), noteList, this)
            rvNoteTodoList.adapter = adapterNoteList
        }
    }

    private fun searchNotes() {
        if (edtSearchField.text.isEmpty()){
            noteControllers.allNotes.observe(viewLifecycleOwner) {noteList ->
                adapterNoteList.updateNoteList(noteList)
            }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                val searchText = edtSearchField.text.toString()
                noteControllers.searchNotes(searchText).observe(viewLifecycleOwner) {noteList ->
                    adapterNoteList.updateNoteList(noteList)
                }
            }
        }
    }

    private fun clearSearchField() {
        edtSearchField.setText("")
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
}
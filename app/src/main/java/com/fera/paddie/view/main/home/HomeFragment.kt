package com.fera.paddie.view.main.home

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
import com.fera.paddie.view.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment(), AdapterNoteList.NoteActivities {

    private lateinit var v: View
    private lateinit var ivSearch: ImageView
    private lateinit var ivClearSearchField: ImageView
    private lateinit var ivAddNote: ImageView //Buttons
    private lateinit var edtSearchField: EditText
    private lateinit var ivShowSideDrawer: ImageView

    //######### NOTEs & TODOs List PROPERTY #########//
    private lateinit var rvNoteList: RecyclerView
    private lateinit var adapterNoteList: AdapterNoteList

    //######### CONTROLLERS PROPERTY #########//
    private lateinit var noteControllers: NoteControllers
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_home, container, false)

        initViews()
        addActionListeners()

        return v
    }

    private fun addActionListeners() {
        ivAddNote.setOnClickListener {
            findNavController().navigate(R.id.addNoteFragment)
        }
        ivShowSideDrawer.setOnClickListener {
            val parentActivity = activity as MainActivity
            parentActivity.showHideSideDrawer()
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
        ivShowSideDrawer = v.findViewById(R.id.ivShowSideDrawer)

        //######### CONTROLLERS #########//
        noteControllers = NoteControllers(requireActivity().application)

        //######### RECYCLER VIEWS #########//
        rvNoteList = v.findViewById(R.id.rvNoteList_home)
        rvNoteList.layoutManager = LinearLayoutManager(requireContext())
        noteControllers.allNotes.observe(viewLifecycleOwner) {noteList ->
            adapterNoteList = AdapterNoteList(requireContext(), noteList, this)
            rvNoteList.adapter = adapterNoteList
        }
    }

    override fun navigateToAddNoteFragment(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val tblNote = getNote(id)
            val bundle = Bundle().apply {
                putParcelable("tblNote", tblNote)
            }

            withContext(Dispatchers.Main){
                findNavController().navigate(R.id.addNoteFragment, bundle)
            }
        }
    }

    private fun searchNotes() {
        if (edtSearchField.text.isEmpty()){
            noteControllers.allNotes.observe(viewLifecycleOwner) {noteList ->
                adapterNoteList.updateNoteList(noteList)
            }
        } else {
//            CoroutineScope(Dispatchers.IO).launch {
                val searchText = edtSearchField.text.toString()
                noteControllers.searchNotes(searchText).observe(viewLifecycleOwner) {noteList ->
                    adapterNoteList.updateNoteList(noteList)
                }
//            }
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

    override suspend fun getNote(id: Int): TblNote {
        return withContext(Dispatchers.IO){
            noteControllers.getNote(id)
        }
    }
}
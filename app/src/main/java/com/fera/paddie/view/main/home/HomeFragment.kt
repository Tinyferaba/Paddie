package com.fera.paddie.view.main.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fera.paddie.R
//import com.fera.paddie.controller.NoteControllers
import com.fera.paddie.model.TblNote
import com.fera.paddie.model.util.CONST
import com.fera.paddie.view.main.MainActivity
import com.fera.paddie.view.main.addNote.AddNoteActivity
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class HomeFragment : Fragment() {
    private val TAG = "HomeFragment"

    private lateinit var v: View
    private lateinit var ivSearch: ImageView
    private lateinit var ivClearSearchField: ImageView
    private lateinit var ivAddNote: ImageView //Buttons
    private lateinit var edtSearchField: EditText
    private lateinit var ivShowSideDrawer: ImageView

    //######### NOTEs & TODOs List PROPERTY #########//
    private lateinit var rvNoteList: RecyclerView
    private lateinit var adapterNoteList: AdapterNoteList
    private var noteList = mutableListOf<TblNote>()

    //######### CONTROLLERS PROPERTY #########//
//    private lateinit var noteControllers: NoteControllers
    private lateinit var mDBRef: DatabaseReference

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
//        ivAddNote.setOnClickListener {
//            findNavController().navigate(R.id.addNoteFragment)
//        }
//        ivShowSideDrawer.setOnClickListener {
//            val parentActivity = activity as MainActivity
//            parentActivity.showHideSideDrawer()
//        }
//
//        ivClearSearchField.setOnClickListener { clearSearchField() }
//        ivSearch.setOnClickListener { searchNotes() }
//        edtSearchField.addTextChangedListener { searchNotes() }
    }

    private fun initViews() {
//        mDBRef = FirebaseDatabase.getInstance().getReference()

        //######### VIEWS #########//
//        ivSearch = v.findViewById(R.id.ivSearchNoteAndTodo)     //Image Views
//        ivClearSearchField = v.findViewById(R.id.ivClearSearchField)
//        ivAddNote = v.findViewById(R.id.ivAddNote)
//        edtSearchField = v.findViewById(R.id.edtSearchField)
//        ivShowSideDrawer = v.findViewById(R.id.ivShowSideDrawer)

        //######### CONTROLLERS #########//
//        noteControllers = NoteControllers(requireActivity().application)

        //######### RECYCLER VIEWS #########//
//        rvNoteList = v.findViewById(R.id.rvNoteList_home)
//        rvNoteList.layoutManager = LinearLayoutManager(requireContext())
//        adapterNoteList = AdapterNoteList(requireContext(), noteList, this)
//        rvNoteList.adapter = adapterNoteList

//        mDBRef.child(CONST.KEY_TBL_NOTE).addValueEventListener(object : ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//                noteList.clear()
//                for (noteSnapshot in snapshot.children){
//                    val tblNote = noteSnapshot.getValue(TblNote::class.java)!!
//                    noteList.add(tblNote)
//                }
//                adapterNoteList.updateNoteList(noteList)
//                Log.d(TAG, "onDataChange: $noteList")
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//        })
//        noteControllers.allNotes.observe(viewLifecycleOwner) {noteList ->
//            adapterNoteList = AdapterNoteList(requireContext(), noteList, this)
//            rvNoteList.adapter = adapterNoteList
//        }
    }
//
//    override fun navigateToAddNoteFragment(id: String) {
////        CoroutineScope(Dispatchers.IO).launch {
////            val tblNote = getNote(id)
//            var tblNote = TblNote()
//
//            mDBRef.child(CONST.KEY_TBL_NOTE).child(id)
//                .get()
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful){
//                        tblNote = task.result.getValue(TblNote::class.java)!!
//                    } else {
//                        Toast.makeText(requireContext(), "Error Loading the note $id", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
////            val bundle = Bundle().apply {
////                putParcelable(CONST.KEY_TBL_NOTE, tblNote)
////            }
//
////            withContext(Dispatchers.Main){
////                findNavController().navigate(R.id.addNoteFragment, bundle)
////            }
////        }
//    }
//
//    private fun searchNotes() {
//        if (edtSearchField.text.isEmpty()){
////            noteControllers.allNotes.observe(viewLifecycleOwner) {noteList ->
////                adapterNoteList.updateNoteList(noteList)
////            }
//        } else {
////            CoroutineScope(Dispatchers.IO).launch {
//                val searchText = edtSearchField.text.toString()
////                noteControllers.searchNotes(searchText).observe(viewLifecycleOwner) {noteList ->
////                    adapterNoteList.updateNoteList(noteList)
////                }
////            }
//        }
//    }
//
//    private fun clearSearchField() {
//        edtSearchField.setText("")
//    }
//
//    override fun updateNote(tblNote: TblNote) {
////        CoroutineScope(Dispatchers.IO).launch {
////            noteControllers.updateNote(tblNote)
////        }
//        val updates = mapOf<String, Any>(
////            "favourite" to tblNote.isFavourite,
//            "title" to tblNote.title.toString(),
//            "description" to tblNote.description.toString(),
//            "dateCreated" to tblNote.dateCreated,
//            "dateModified" to tblNote.dateModified
//        )
//
//        mDBRef.child(CONST.KEY_TBL_NOTE)
//            .updateChildren(updates)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful){
//                    Toast.makeText(requireContext(), "Updated...", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(requireContext(), "Error Updating...", Toast.LENGTH_SHORT).show()
//                }
//            }
//    }
//
//    override fun deleteNote(id: String) {
////        CoroutineScope(Dispatchers.IO).launch {
////            noteControllers.deleteNote(id)
////        }
//        mDBRef.child(CONST.KEY_TBL_NOTE).child(id).removeValue().addOnCompleteListener {
//            if (it.isSuccessful){
//                Toast.makeText(requireContext(), "Deleted...", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(requireContext(), "Error Deleting...", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    override fun updateFavourite(id: String, isFavourite: Boolean) {
////        CoroutineScope(Dispatchers.IO).launch {
////            noteControllers.updateFavourite(id, isFavourite)
////        }
//        val updates = mapOf<String, Any>(
//            "favourite" to isFavourite,
//            "dateModified" to System.currentTimeMillis()
//        )
//
//        mDBRef.child(CONST.KEY_TBL_NOTE).child(id)
//            .updateChildren(updates)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful){
//                    Toast.makeText(requireContext(), "Updated...", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(requireContext(), "Error updating...", Toast.LENGTH_SHORT).show()
//                }
//            }
//    }
//
//    override suspend fun getNote(id: String): TblNote {
////        return withContext(Dispatchers.IO){
////            noteControllers.getNote(id)
////        }
//        var tblNote = TblNote()
//
//        mDBRef.child(CONST.KEY_TBL_NOTE).child(id)
//            .get()
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful){
//                    tblNote = task.result.getValue(TblNote::class.java)!!
//                } else {
//                    Toast.makeText(requireContext(), "Error loading note: $id", Toast.LENGTH_SHORT).show()
//                }
//            }
//        return tblNote
//    }
}
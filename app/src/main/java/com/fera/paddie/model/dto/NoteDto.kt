package com.fera.paddie.model.dto

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.fera.paddie.model.TblNote
import com.fera.paddie.model.util.CONST
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class NoteDto(private val application: Application) {
    private val TAG = "NoteDto"
    private val noteCol = Firebase.firestore.collection(CONST.KEY_TBL_NOTE)

    fun saveOrUpdateNote(note: TblNote){
        noteCol.document(note.pkNoteId.toString())
            .set(note)
            .addOnSuccessListener {
                Toast.makeText(application.applicationContext, "Saved Successfully...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {exception ->
                Toast.makeText(application.applicationContext, "Failed to save to Firestore!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "saveNote: $exception")
            }
    }

    fun deleteNote(id: Int){
        noteCol.document(id.toString()).delete()
            .addOnSuccessListener {
                Toast.makeText(application.applicationContext, "Deleted Successfully...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {exception ->
                Toast.makeText(application.applicationContext, "Failed to delete...", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "deleteNote: $exception")
            }
    }

    fun getAllNotes(): List<TblNote> {
        noteCol.document(CONST.KEY_TBL_NOTE)
            .get()
            .addOnSuccessListener {documentSnapshot ->

            }
            .addOnFailureListener {exception ->
                Toast.makeText(application.applicationContext, "Failed to delete...", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "deleteNote: $exception")
            }

        return mutableListOf()
    }

    fun searchNote(text: String): List<TblNote> {
        return mutableListOf()
    }
}
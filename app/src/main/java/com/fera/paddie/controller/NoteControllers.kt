package com.fera.paddie.controller

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.fera.paddie.model.TblNote
import com.fera.paddie.model.database.NoteDatabase


class NoteControllers (application: Application): AndroidViewModel(application) {
    private val noteDao = NoteDatabase.getDatabase(application).noteDao()


    //TODO: Write dto

    val allNotes: LiveData<List<TblNote>> = noteDao.getAllNotes()

    //######### QUERY #########//
    val getAllNewNotes = noteDao.getAllNewNotes()
    val getAllUploadedNotes = noteDao.getAllUploadedNotes()

    fun getNote(id: Int): TblNote {
        return noteDao.getNote(id)
    }

    //######### NOTE METHODS #########//
    suspend fun insertNote(tblNote: TblNote): Int {
        //  withContext blocks the caller until this function completes execution and returns
        //  the result only then can other threads under it run
        return withContext(Dispatchers.IO){
            noteDao.insert(tblNote).toInt()
        }
    }

    fun searchNotes(searchText: String): List<TblNote>{
        return noteDao.searchNotes(searchText)
    }

    suspend fun updateNote(tblNote: TblNote){
        noteDao.update(tblNote)
    }

    suspend fun updateFavourite(id: Int, isFavourite: Boolean){
        noteDao.updateFavourite(id, isFavourite)
    }

    suspend fun deleteNote(id: Int){
        noteDao.delete(id)
    }

}
package com.fera.paddie.controller

//import android.app.Application
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.LiveData
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import com.fera.paddie.model.TblNote
//import com.fera.paddie.model.database.NoteTodoDatabase
//import com.fera.paddie.model.dto.NoteDto
//
//
//class NoteControllers (application: Application): AndroidViewModel(application) {
//    private val noteDao = NoteTodoDatabase.getDatabase(application).noteDao()
//    private val noteDto = NoteDto(application)
//
//    //TODO: Write dto
//
//    val allNotes: LiveData<List<TblNote>> = noteDao.getAllNotes()
//
//    //######### QUERY #########//
//    fun getNote(id: Int): TblNote {
//        return noteDao.getNote(id)
//    }
//
//    //######### NOTE METHODS #########//
//    suspend fun insertNote(tblNote: TblNote): Int {
//        //  withContext blocks the caller until this function completes execution and returns
//        //  the result only then can other threads under it run
//        return withContext(Dispatchers.IO){
//            noteDao.insert(tblNote).toInt()
//        }
//    }
//
//    fun searchNotes(searchText: String): LiveData<List<TblNote>>{
//        return noteDao.searchNotes(searchText)
//    }
//
//    suspend fun updateNote(tblNote: TblNote){
//        noteDao.update(tblNote)
//    }
//
//    suspend fun updateFavourite(id: Int, isFavourite: Boolean){
//        noteDao.updateFavourite(id, isFavourite)
//    }
//
//    suspend fun deleteNote(id: Int){
//        noteDao.delete(id)
//    }
//
//}
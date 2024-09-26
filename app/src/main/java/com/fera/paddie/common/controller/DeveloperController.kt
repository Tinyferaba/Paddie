package com.fera.paddie.common.controller

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.fera.paddie.common.model.TblDevelopers
import com.fera.paddie.common.model.TblUser
import com.fera.paddie.common.model.database.NoteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeveloperController(application: Application): AndroidViewModel(application) {
    private val devDao = NoteDatabase.getDatabase(application).developerDao()

    //######### QUERIES #########//

    suspend fun getDeveloper(id: String): TblDevelopers {
        return devDao.getDeveloperById(id)
    }

    fun getAllDevelopers():List<TblDevelopers> {
        return devDao.getAllDevelopers()
    }

    //######### USER #########//
    suspend fun insertDeveloper(user: TblDevelopers) {
        devDao.insertDeveloper(user)
    }

    suspend fun updateDeveloper(user: TblDevelopers){
        devDao.updateDeveloper(user)
    }

    suspend fun deleteDeveloper(id: String){
        devDao.deleteDeveloper(id)
    }
}
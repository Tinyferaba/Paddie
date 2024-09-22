package com.fera.paddie.controller

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.fera.paddie.model.TblUser
import com.fera.paddie.model.database.NoteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserController(application: Application): AndroidViewModel(application) {
    private val userDao = NoteDatabase.getDatabase(application).userDao()

    //######### AUTHENTICATE #########//
    suspend fun checkUser(username: String, password: String): Int {
        return userDao.checkUser(username, password)
    }

    //######### QUERIES #########//
    suspend fun getUser(username: String, password: String): TblUser {
        return userDao.getUser(username, password)
    }

    suspend fun getUser(id: Int): TblUser {
        return userDao.getUser(id)
    }
    suspend fun checkUserByUid(uid: String): Int {
        return userDao.checkUserByUid(uid)
    }

    fun getAllUsers(): LiveData<List<TblUser>> {
        return userDao.getAllUsers()
    }

    //######### USER #########//
    suspend fun insertUser(user: TblUser): Int {
        return withContext(Dispatchers.IO){
            userDao.insertUser(user).toInt()
        }
    }

    suspend fun updateUser(user: TblUser){
        userDao.updateUser(user)
    }

    suspend fun deleteUser(id: Int){
        userDao.deleteUser(id)
    }
}
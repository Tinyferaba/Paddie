package com.fera.paddie.common.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fera.paddie.common.model.TblUser

@Dao
interface UserDao {
    //######### QUERYING HERE #########//
    @Query("""select * from tbl_user where email = :userEmail and password = :userPassword limit 1""")
    suspend fun getUser(userEmail: String, userPassword: String): TblUser

    @Query("""select * from tbl_user where pkUserId = :id""")
    suspend fun getUser(id: Int): TblUser

    @Query("""select count(*) from tbl_user where uid = :uid""")
    suspend fun checkUserByUid(uid: String): Int

    @Query("""select * from tbl_user where pkUserId = :id""")
    suspend fun getUserById(id: Int): TblUser

    @Query("Select * from tbl_user")
    fun getAllUsers(): LiveData<List<TblUser>>

    @Query("select Count(*) from tbl_user where email = :userEmail and password = :userPassword limit 1")
    suspend fun checkUser(userEmail: String, userPassword: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: TblUser): Long

    @Update
    suspend fun updateUser(user: TblUser)

    @Query("delete from tbl_user where pkUserId = :id")
    suspend fun deleteUser(id: Int)
}
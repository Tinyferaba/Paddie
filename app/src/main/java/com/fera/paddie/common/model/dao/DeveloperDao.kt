package com.fera.paddie.common.model.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fera.paddie.common.model.TblDevelopers

@Dao
interface DeveloperDao {
    //######### QUERYING HERE #########//
    @Query("""select * from tbl_developers where stdId = :stdId""")
    suspend fun getDeveloper(stdId: String): TblDevelopers

    @Query("""select * from tbl_developers where stdId = :stdId""")
    suspend fun getDeveloperById(stdId: String): TblDevelopers

    @Query("Select * from tbl_developers")
    fun getAllDevelopers(): List<TblDevelopers>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeveloper(user: TblDevelopers)

    @Update
    suspend fun updateDeveloper(user: TblDevelopers)

    @Query("delete from tbl_developers where stdId = :stdId")
    suspend fun deleteDeveloper(stdId: String)
}
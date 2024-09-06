package com.fera.paddie.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fera.paddie.model.TblNote

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tblNote: TblNote): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(tblNote: TblNote)

    @Query("update tbl_note set favourite = :isFavourite, updated = 1 where pkNoteId = :id")
    suspend fun updateFavourite(id: Int, isFavourite: Boolean)

    @Query("delete from tbl_note where pkNoteId = :id")
    suspend fun delete(id: Int)

    //######### RETRIEVE OPERATIONS #########//
    @Query("""select * from tbl_note""")
    fun getAllNotes(): LiveData<List<TblNote>>

    @Query("""select * from tbl_note where `key` is null or updated = 1""")
    fun getAllNewNotes(): LiveData<List<TblNote>>

    @Query("""select * from tbl_note where `key` is not null and updated = 0""")
    fun getAllUploadedNotes(): LiveData<List<TblNote>>

    @Query("""
        select * from tbl_note as N
            where N.title like '%' || :searchText || '%'
                or N.description like '%' || :searchText || '%'
    """)
    fun searchNotes(searchText: String): List<TblNote>

    @Query("select * from tbl_note where pkNoteId = :id")
    fun getNote(id: Int): TblNote
}


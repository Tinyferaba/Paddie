package com.fera.paddie.common.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fera.paddie.common.model.TblNote

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

    @Query("select * from tbl_note order by title asc")
    fun sortByTitleAsc(): List<TblNote>

    @Query("select * from tbl_note order by title desc")
    fun sortByTitleDesc(): List<TblNote>

    @Query("""
        select * from tbl_note where favourite = 1
        order by title asc
    """)
    fun getAllFavHymnsByTitleASC(): LiveData<List<TblNote>>

    @Query("""
        select * from tbl_note where favourite = 1
        order by title desc
    """)
    fun getAllFavHymnsByTitleDESC(): LiveData<List<TblNote>>

    @Query("""
        select * from tbl_note
        order by title asc
    """)
    fun getAllHymnsByTitleASC(): LiveData<List<TblNote>>

    @Query("""
        select * from tbl_note
        order by title desc
    """)
    fun getAllHymnsByTitleDESC(): LiveData<List<TblNote>>

    @Query("""
        select * from tbl_note where favourite = 1
        order by description asc
    """)
    fun getAllFavByDescASC(): LiveData<List<TblNote>>

    @Query("""
        select * from tbl_note where favourite = 1
        order by description desc
    """)
    fun getAllFavByDescDESC(): LiveData<List<TblNote>>

    @Query("""
        select * from tbl_note
        order by description asc
    """)
    fun getAllByDescASC(): LiveData<List<TblNote>>

    @Query("""
        select * from tbl_note
        order by description desc
    """)
    fun getAllByDescDESC(): LiveData<List<TblNote>>

}


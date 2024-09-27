package com.fera.paddie.common.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fera.paddie.common.model.TblDevelopers
import com.fera.paddie.common.model.TblNote
import com.fera.paddie.common.model.TblUser
import com.fera.paddie.common.model.dao.DeveloperDao
import com.fera.paddie.common.model.dao.NoteDao
import com.fera.paddie.common.model.dao.UserDao
import com.fera.paddie.common.model.typeConverters.Converters


@Database(
    entities = [TblNote::class, TblUser::class, TblDevelopers::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(value = [Converters::class])
abstract class NoteDatabase: RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun userDao(): UserDao
    abstract fun developerDao(): DeveloperDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase?=null

        fun getDatabase(context: Context): NoteDatabase {
            val tempINSTANCE = INSTANCE
            if (tempINSTANCE != null)
                return tempINSTANCE

            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_todo_db")
//                    .createFromAsset("database/note_todo_db.db")
                    .build()

                INSTANCE = instance
                return instance
            }
        }
    }
}
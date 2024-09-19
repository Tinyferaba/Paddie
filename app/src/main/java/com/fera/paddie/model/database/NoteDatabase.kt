package com.fera.paddie.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fera.paddie.model.TblDevelopers
import com.fera.paddie.model.TblNote
import com.fera.paddie.model.TblUser
import com.fera.paddie.model.dao.DeveloperDao
import com.fera.paddie.model.dao.NoteDao
import com.fera.paddie.model.dao.UserDao
import com.fera.paddie.model.typeConverters.Converters


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
                    "note_todo_db"
                ).build()

                INSTANCE = instance
                return instance
            }
        }
    }
}
package com.fera.paddie.model.database
//
//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//import androidx.room.TypeConverters
//import com.fera.paddie.model.TblNote
//import com.fera.paddie.model.dao.NoteDao
//import com.fera.paddie.model.typeConverters.DateConverter
//
//
//@Database(
//    entities = [TblNote::class],
//    version = 1,
//    exportSchema = true
//)
//@TypeConverters(value = [DateConverter::class])
//abstract class NoteTodoDatabase: RoomDatabase() {
//    abstract fun noteDao(): NoteDao
//
//    companion object {
//        @Volatile
//        private var INSTANCE: NoteTodoDatabase?=null
//
//        fun getDatabase(context: Context): NoteTodoDatabase {
//            val tempINSTANCE = INSTANCE
//            if (tempINSTANCE != null)
//                return tempINSTANCE
//
//            synchronized(this){
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    NoteTodoDatabase::class.java,
//                    "note_todo_db"
//                ).build()
//
//                INSTANCE = instance
//                return instance
//            }
//        }
//    }
//}
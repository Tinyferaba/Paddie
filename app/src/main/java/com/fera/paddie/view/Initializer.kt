package com.fera.paddie.view

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.fera.paddie.R
import com.fera.paddie.controller.DeveloperController
import com.fera.paddie.model.TblDevelopers
import com.fera.paddie.util.CONST
import com.fera.paddie.view.main.home.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Initializer {
    companion object {

        fun initApp(mainActivity: MainActivity){
            if (isFirstRun(mainActivity.application)){
                //######### Put RUN ONCE codes Here #########//

                CoroutineScope(Dispatchers.IO).launch {
//                    loadDemoData(mainActivity)
                }

                markAsRan(mainActivity.application)
            }
        }

        private fun markAsRan(application: Application) {
            val editor = application.getSharedPreferences(CONST.SHARED_PREF_db, AppCompatActivity.MODE_PRIVATE).edit()
            editor.putBoolean(CONST.IS_FIRST_RUN, false)
            editor.apply()
        }

        private fun isFirstRun(application: Application): Boolean {
            val shared = application.getSharedPreferences(CONST.SHARED_PREF_db, AppCompatActivity.MODE_PRIVATE)
            val isRun = shared.getBoolean(CONST.IS_FIRST_RUN, true)
            return isRun
        }

        private suspend fun loadDemoData(mainActivity: MainActivity) {
            val photo1 = ContextCompat.getDrawable(mainActivity.baseContext, R.drawable.killmonger)
            val photo2 = ContextCompat.getDrawable(mainActivity.baseContext, R.drawable.antman)
            val photo3 = ContextCompat.getDrawable(mainActivity.baseContext, R.drawable.storm)
            val photo4 = ContextCompat.getDrawable(mainActivity.baseContext, R.drawable.wonder_women)

            val devList = listOf(
                TblDevelopers("22302067", "Noody", "HAGAYO", "22302067noha@student.pnguot.ac.pg", getBitmap(photo1!!)),
                TblDevelopers("22302074", "Boi", "KOBLA", "22302074boko@student.pnguot.ac.pg", getBitmap(photo2!!)),
                TblDevelopers("22301822", "Shima Delilah", "DENSON", "22301822shde@student.pnguot.ac.pg", getBitmap(photo3!!)),
                TblDevelopers("22301788", "Renee", "ATTMANKIA", "22301788reat@student.pnguot.ac.pg", getBitmap(photo4!!))
            )

            val uploadScope = CoroutineScope(Dispatchers.IO)
            val devControllers = ViewModelProvider(mainActivity)[DeveloperController::class.java]

            val uploadJob = devList.map { tblDevelopers ->
                uploadScope.launch {
                    devControllers.insertDeveloper(tblDevelopers)
                }
            }
            uploadJob.forEach { job -> job.join()}
        }

        private fun getBitmap(drawable: Drawable): Bitmap {
            return (drawable as BitmapDrawable).bitmap
        }
    }
}
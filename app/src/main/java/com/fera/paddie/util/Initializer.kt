package com.fera.paddie.util

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import com.fera.paddie.main.MainActivity

class Initializer {
    companion object {

        fun initApp(mainActivity: MainActivity) {
            if (isFirstRun(mainActivity.application)) {
                //######### Put RUN ONCE codes Here #########//

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
    }
}
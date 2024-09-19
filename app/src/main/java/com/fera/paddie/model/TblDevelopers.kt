package com.fera.paddie.model

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("tbl_developers")
data class TblDevelopers(
    @PrimaryKey(autoGenerate = false)
    val stdId: String,
    val firstName: String?=null,
    val lastName: String?=null,
    val email: String?=null,
    val profilePhoto: Bitmap?=null
)

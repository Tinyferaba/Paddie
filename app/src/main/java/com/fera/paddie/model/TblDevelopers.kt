package com.fera.paddie.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("tbl_developers")
data class TblDevelopers(
    @PrimaryKey(autoGenerate = false)
    val stdId: String,
    val firstName: String,
    val lastName: String,
    val email: String
)

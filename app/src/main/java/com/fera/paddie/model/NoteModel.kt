package com.fera.paddie.model

import android.os.Parcel
import android.os.Parcelable
//import androidx.room.ColumnInfo
//import androidx.room.Entity
//import androidx.room.PrimaryKey
import java.util.Date


//@Entity(tableName = "tbl_note")
data class TblNote (
//    @PrimaryKey(autoGenerate = true)
//    @ColumnInfo(name = "pkNoteId")
    var pkNoteTodoId: String?,
//    var isFavourite: Boolean,
    var title: String?,
    var description: String?,
    var dateCreated: Long,
    var dateModified: Long
): Parcelable {
    constructor() : this(
        pkNoteTodoId = null,
//        isFavourite = false,
        title = null,
        description = null,
        dateCreated = Date().time,
        dateModified = Date().time
    )

    constructor(parcel: Parcel) : this(
        pkNoteTodoId = parcel.readString().toString(),
//        isFavourite = parcel.readByte() != 0.toByte(),
        title = parcel.readString(),
        description = parcel.readString(),
        dateCreated = parcel.readLong(),
        dateModified =  parcel.readLong()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(pkNoteTodoId)
//        parcel.writeByte(if (isFavourite) 1 else 0)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeLong(dateCreated)
        parcel.writeLong(dateModified)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TblNote> {
        override fun createFromParcel(parcel: Parcel): TblNote {
            return TblNote(parcel)
        }

        override fun newArray(size: Int): Array<TblNote?> {
            return arrayOfNulls(size)
        }
    }
}

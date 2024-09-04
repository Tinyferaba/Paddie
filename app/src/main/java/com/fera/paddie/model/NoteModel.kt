package com.fera.paddie.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_note")
data class TblNote(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pkNoteId")
    var pkNoteId: Int? = null,
    var key: String? = null,
    var title: String? = null,
    var description: String? = null,
    var favourite: Boolean = false,
    var dateCreated: Long = System.currentTimeMillis(),
    var dateModified: Long = System.currentTimeMillis()
) : Parcelable {

    // Empty constructor
    constructor() : this(
        pkNoteId = null,
        key = null,
        title = null,
        description = null,
        favourite = false,
        dateCreated = System.currentTimeMillis(),
        dateModified = System.currentTimeMillis()
    )

    // Parcelable constructor
    constructor(parcel: Parcel) : this(
        pkNoteId = parcel.readValue(Int::class.java.classLoader) as? Int,
        key = parcel.readString(),
        title = parcel.readString(),
        description = parcel.readString(),
        favourite = parcel.readByte() != 0.toByte(),
        dateCreated = parcel.readLong(),
        dateModified = parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(pkNoteId)
        parcel.writeString(key)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeByte(if (favourite) 1 else 0)
        parcel.writeLong(dateCreated)
        parcel.writeLong(dateModified)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<TblNote> {
        override fun createFromParcel(parcel: Parcel): TblNote = TblNote(parcel)
        override fun newArray(size: Int): Array<TblNote?> = arrayOfNulls(size)
    }
}
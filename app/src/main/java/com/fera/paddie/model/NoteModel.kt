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
    var pkNoteId: Int? = null,  // Changed to Int? for auto-generation
    var key: String?,
    var favourite: Boolean = false,
    var title: String? = null,
    var description: String? = null,
    var dateCreated: Long = System.currentTimeMillis(),
    var dateModified: Long = System.currentTimeMillis()
) : Parcelable {

    constructor(): this(
        pkNoteId = null,
        key = null,
        favourite = false,
        title = null,
        description = null,
        dateCreated = System.currentTimeMillis(),
        dateModified = System.currentTimeMillis()
    )

    // Parcelable constructor
    constructor(parcel: Parcel) : this(
        pkNoteId = parcel.readValue(Int::class.java.classLoader) as? Int,
        favourite = parcel.readByte() != 0.toByte(),
        key = parcel.readString(),
        title = parcel.readString(),
        description = parcel.readString(),
        dateCreated = parcel.readLong(),
        dateModified = parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(pkNoteId)
        parcel.writeByte(if (favourite) 1 else 0)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeLong(dateCreated)
        parcel.writeLong(dateModified)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<TblNote> {
        override fun createFromParcel(parcel: Parcel): TblNote = TblNote(parcel)
        override fun newArray(size: Int): Array<TblNote?> = arrayOfNulls(size)
    }
}

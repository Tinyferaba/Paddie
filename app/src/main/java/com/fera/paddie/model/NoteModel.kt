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
    var dateModified: Long = System.currentTimeMillis(),
    var updated: Boolean = false
) : Parcelable {

    // Empty constructor
    constructor() : this(
        pkNoteId = null,
        key = null,
        title = null,
        description = null,
        favourite = false,
        dateCreated = System.currentTimeMillis(),
        dateModified = System.currentTimeMillis(),
        updated = false
    )

    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(pkNoteId)
        parcel.writeString(key)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeByte(if (favourite) 1 else 0)
        parcel.writeLong(dateCreated)
        parcel.writeLong(dateModified)
        parcel.writeByte(if (updated) 1 else 0)
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
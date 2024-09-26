package com.fera.paddie.common.model

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("tbl_user")
data class TblUser(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("pkUserId")
    val pkUserId: Int = 0,
    val uid: String?=null,
    val firstName: String?=null,
    val middleName: String?=null,
    val lastName: String?=null,
    val email: String?=null,
    val password: String?=null,
    val gender: String?=null,
    val photo: String?=null,
    val registeredDate: Long?=null
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(pkUserId)
        parcel.writeString(uid)
        parcel.writeString(firstName)
        parcel.writeString(middleName)
        parcel.writeString(lastName)
        parcel.writeString(email)
        parcel.writeString(password)
        parcel.writeString(gender)
        parcel.writeString(photo)
        parcel.writeValue(registeredDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TblUser> {
        override fun createFromParcel(parcel: Parcel): TblUser {
            return TblUser(parcel)
        }

        override fun newArray(size: Int): Array<TblUser?> {
            return arrayOfNulls(size)
        }
    }
}
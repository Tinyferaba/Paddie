package com.fera.paddie.model

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
    val uid: String?,
    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
    val email: String?,
    val password: String?,
    val gender: String?,
    val photo: Bitmap?,
    val registeredDate: Long?
): Parcelable {
    constructor(): this(
        pkUserId = 0,
        uid = null,
        firstName = null,
        middleName = null,
        lastName = null,
        password = null,
        email = null,
        gender = null,
        photo = null,
        registeredDate = null
    )

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(Bitmap::class.java.classLoader),
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
        parcel.writeParcelable(photo, flags)
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
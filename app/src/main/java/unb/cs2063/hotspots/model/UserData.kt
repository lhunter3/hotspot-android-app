package unb.cs2063.hotspots.model

import android.os.Parcel
import android.os.Parcelable

data class UserData(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var uri: String? = "",
    var likes: Int = 0,
    var dislikes: Int = 0,
    var distance: Double = 99.9
    //think about adding timestamp, could be used to check 24hr. also display how long ago image was posted (ie 55min ago..)
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readDouble()

    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(uri)
        parcel.writeInt(likes)
        parcel.writeInt(dislikes)
        parcel.writeDouble(distance)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserData> {
        override fun createFromParcel(parcel: Parcel): UserData {
            return UserData(parcel)
        }

        override fun newArray(size: Int): Array<UserData?> {
            return arrayOfNulls(size)
        }
    }
}



package unb.cs2063.hotspots.model

import android.os.Parcel
import android.os.Parcelable
import java.util.UUID

data class UserData(
    var id: String = UUID.randomUUID().toString(),
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var uri: String? = "",
    var likes: Int = 0,
    var dislikes: Int = 0,
    var distance: Double = 99.9,
    var uploadDate: Long = 0

) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readDouble(),
        parcel.readLong()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(uri)
        parcel.writeInt(likes)
        parcel.writeInt(dislikes)
        parcel.writeDouble(distance)
        parcel.writeLong(uploadDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getTimeAgo() : String{

        val ts = System.currentTimeMillis() - this.uploadDate

        val seconds = ts / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "$days Days Ago"
            hours > 0 -> "$hours Hours Ago"
            minutes > 0 -> "$minutes Minutes Ago"
            else -> "Just Now"
        }
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



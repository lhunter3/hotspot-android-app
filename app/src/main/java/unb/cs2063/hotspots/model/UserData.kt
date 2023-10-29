package unb.cs2063.hotspots.model

import com.google.firebase.firestore.GeoPoint
import java.io.Serializable

data class UserData(
    val latLong :GeoPoint = GeoPoint(0.0,0.0),
    val uri: String = ""
) : Serializable



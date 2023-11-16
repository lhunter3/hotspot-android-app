package unb.cs2063.hotspots.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import unb.cs2063.hotspots.model.UserData

class FireBaseUtil {

    private val storage = FirebaseFirestore.getInstance()
    private val collection = storage.collection(collectionName)

    fun updateUserData(userData: UserData) {

        //finding the image in collection.
        val query = collection.whereEqualTo("uri", userData.uri)

        //updating image stats.
        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                for (document in task.result) {
                    document.reference.update(
                        "likes", userData.likes,
                        "dislikes", userData.dislikes
                    ).addOnSuccessListener {
                       Log.i(TAG, "Updated image stats")
                    }.addOnFailureListener { e ->
                        Log.e(TAG, "Error updating image stats", e)
                    }
                }
            }
        }
    }

    fun getUserData(callback: (List<UserData>) -> Unit) {

        collection.get()
            .addOnCompleteListener(OnCompleteListener { task ->
                if (task.isSuccessful) {
                    val resultList = ArrayList<UserData>()
                    for (document in task.result) {
                        //convert document to userdata
                        val data = document.toObject(UserData::class.java)
                        Log.i(TAG,data.toString())
                        resultList.add(data)
                    }
                    callback(resultList)
                } else {
                    callback(emptyList())
                }
            })
    }

    fun pushUserData(activity: Activity, uri: Uri) {
        val storage = FirebaseStorage.getInstance()
        val imagesRef = storage.reference.child("images/${uri.lastPathSegment}")

        imagesRef.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                imagesRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        val downloadUrl = uri.toString()

                        if (checkLocationPermission(activity)) {
                            getLocation(activity, { location ->
                                if (location != null) {
                                    val data = mapOf(
                                        "latitude" to location.latitude,
                                        "longitude" to location.longitude,
                                        "uri" to downloadUrl,
                                        "likes" to 0,
                                        "dislikes" to 0,
                                        "uploadDate" to System.currentTimeMillis()
                                    )

                                    collection
                                        .add(data)
                                        .addOnSuccessListener {
                                            Log.d(TAG, "Pushed User Data: ${location.longitude}, ${location.latitude}, $downloadUrl successfully written!")
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.w(TAG, "Error writing document: ${exception.message}")
                                        }
                                }
                            })
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, exception.message.toString())
                    }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, exception.message.toString())
            }
    }

    private fun checkLocationPermission(activity: Activity): Boolean {
        return (ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
    }
    private fun getLocation(activity: Activity, callback: (Location?) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        if(checkLocationPermission(activity))
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    callback(location)
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting location: ${exception.message}")
                    callback(null)
                }
    }

    companion object{
        private const val TAG = "FireBaseUtil"
        private const val collectionName = "data"
    }

}
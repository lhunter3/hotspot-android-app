package unb.cs2063.hotspots.ui.map


import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import unb.cs2063.hotspots.R
import java.util.Random

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        getAllDataFromFirestore("data") { dataList ->
            // Handle the retrieved data (dataList) here
            for (data in dataList) {
                Log.d(TAG,data.toString())
            }
        }

        return view
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN

        // Request location permissions if not granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            // Enable the "My Location" button on the map
            googleMap.isMyLocationEnabled = true
            val locationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            locationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    val initialLatLng = LatLng(userLocation.latitude, userLocation.longitude)
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(initialLatLng, DEFAULT_ZOOM)
                    googleMap.moveCamera(cameraUpdate)
                }
                else{
                    val initialLatLng = LatLng(YourDefaultLatitude, YourDefaultLongitude)
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(initialLatLng, DEFAULT_ZOOM)
                    googleMap.moveCamera(cameraUpdate)
                }
            }

            // Set up a camera move listener to detect camera position changes
            googleMap.setOnCameraMoveListener {
                // Get the updated camera position
                val cameraPosition = googleMap.cameraPosition
                val latitude = cameraPosition.target.latitude
                val longitude = cameraPosition.target.longitude

                Log.d(TAG, latitude.toString())
                Log.d(TAG, longitude.toString())
                // You can use the latitude and longitude to perform actions when the camera moves
                // For example, fetch new data based on the updated location, update markers, etc.
            }


            val bounds = LatLngBounds(
                LatLng(-20.0, 40.0),   // Southwest corner
                LatLng(0.0, 20.5)     // Northeast corner
            )

            addRandomHeatmapToMap(googleMap,100,bounds)
        }
    }


    fun addRandomHeatmapToMap(googleMap: GoogleMap, numPoints: Int, bounds: LatLngBounds) {
        val heatmapData = ArrayList<LatLng>()
        val random = Random()

        for (i in 0 until numPoints) {
            val randomLat = bounds.southwest.latitude + (bounds.northeast.latitude - bounds.southwest.latitude) * random.nextDouble()
            val randomLng = bounds.southwest.longitude + (bounds.northeast.longitude - bounds.southwest.longitude) * random.nextDouble()

            heatmapData.add(LatLng(randomLat, randomLng))
        }

        // Create a HeatmapTileProvider with the random points
        val heatmapProvider = HeatmapTileProvider.Builder()
            .data(heatmapData)
            .radius(50) // Radius of influence for each data point
            .build()

        // Set the gradient color for the heatmap (optional)
        heatmapProvider.setGradient(Gradient(
            intArrayOf(Color.rgb(102, 225, 0), Color.rgb(255, 0, 0)), // Color gradient
            floatArrayOf(0.2f, 1.0f) // Gradient positions
        ))

        // Add the heatmap layer to the map
        val tileOverlay = googleMap.addTileOverlay(
            TileOverlayOptions().tileProvider(heatmapProvider)
        )
    }

    fun getAllDataFromFirestore(collectionName: String, callback: (List<UserData>) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val collection = firestore.collection(collectionName)

        collection.get()
            .addOnCompleteListener(OnCompleteListener { task ->
                if (task.isSuccessful) {
                    val resultList = ArrayList<UserData>()
                    for (document in task.result) {
                        // Convert Firestore document to your data model
                        val yourData = document.toObject(UserData::class.java)
                        resultList.add(yourData)
                    }
                    callback(resultList)
                } else {
                    // Handle the error
                    callback(emptyList()) // or another suitable error handling
                }
            })
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val DEFAULT_ZOOM = 5.0f
        private const val YourDefaultLatitude = 47.23890841033145
        private const val YourDefaultLongitude = -68.16202752292156
        private const val TAG = "MapFragment"
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}


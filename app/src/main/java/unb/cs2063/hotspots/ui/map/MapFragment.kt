package unb.cs2063.hotspots.ui.map


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.heatmaps.HeatmapTileProvider
import unb.cs2063.hotspots.R
import unb.cs2063.hotspots.model.UserData
import unb.cs2063.hotspots.ui.info.RecyclerDetailActivity
import unb.cs2063.hotspots.utils.FireBaseUtil
import java.io.Serializable
import java.lang.Math.atan2
import java.lang.Math.cos
import java.lang.Math.sin
import java.lang.Math.sqrt
import kotlin.math.pow

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private var FireBaseUtil : FireBaseUtil = FireBaseUtil()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return view
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN

        FireBaseUtil.getFirestoreData("data") { dataList ->
            setHeatMap(googleMap,dataList)
            googleMap.setOnMapClickListener { latLng ->


                //start activity
                val test = getPictureData(latLng,dataList)
                Log.d(TAG,test.toString())

                startImageActivity(test)

            }
        }



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
            }
        }
    }

    private fun getPictureData(latLng: LatLng, data: List<UserData>): List<UserData> {
        val nearbyData = ArrayList<UserData>()

        for (userData in data) {
            val userDataLatLng = LatLng(userData.latLong.latitude, userData.latLong.longitude)
            val distance = calculateDistance(latLng, userDataLatLng)
            Log.d(TAG,distance.toString())
            // Check if the distance is within the threshold of 1.5km
            if (distance <= 1.5) {
                nearbyData.add(userData)
            }
        }

        return nearbyData
    }

    private fun calculateDistance(latLng1: LatLng, latLng2: LatLng): Double {
        // Calculate the distance between two LatLng points using the Haversine formula
        val radiusOfEarth = 6371 // Earth's radius in kilometers

        val lat1 = Math.toRadians(latLng1.latitude)
        val lon1 = Math.toRadians(latLng1.longitude)
        val lat2 = Math.toRadians(latLng2.latitude)
        val lon2 = Math.toRadians(latLng2.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return radiusOfEarth * c
    }

    private fun setHeatMap(googleMap: GoogleMap, data: List<UserData>) {
        // Create a list of WeightedLatLng from your UserData objects
        val heatmapData = ArrayList<LatLng>()
        for(d in data){
            heatmapData.add(LatLng(d.latLong.latitude,d.latLong.longitude))
        }




        val heatmapTileProvider = HeatmapTileProvider.Builder()
            .data(heatmapData)
            .radius(30)
            .build()

        // Add the heatmap layer to the map
        googleMap.addTileOverlay(TileOverlayOptions().tileProvider(heatmapTileProvider))
    }

    private fun startImageActivity(userData: List<UserData>){

        val intent = Intent(requireActivity(), RecyclerDetailActivity::class.java)
        //intent.putExtra("userDataList", ArrayList(userData))

        startActivity(intent)

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


package unb.cs2063.hotspots.ui.map


import android.Manifest
import android.content.Intent
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
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import unb.cs2063.hotspots.R
import unb.cs2063.hotspots.model.UserData
import unb.cs2063.hotspots.utils.FireBaseUtil
import java.lang.Math.atan2
import java.lang.Math.cos
import java.lang.Math.sin
import java.lang.Math.sqrt
import kotlin.math.pow

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private var firebase : FireBaseUtil = FireBaseUtil()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return view
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.custom_map))
        firebase.getUserData() { dataList ->

            if(dataList.isNotEmpty()){
                //sets heatmap with firebase data
                setHeatMap(googleMap,dataList)

                //displays the correct images when heatmap is clicked.
                googleMap.setOnMapClickListener { latLng ->
                    displayImages(latLng,dataList)
                }
            }
            else{
                Log.d(TAG,"Database has no entries. Heatmap cannot be set.")
            }
        }


        //Request location permissions if not granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {

            //Enable the location button on the map
            googleMap.isMyLocationEnabled = true


            val locationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            locationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                //if location is found, move the camera to the location.
                if (location != null) {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    val initialLatLng = LatLng(userLocation.latitude, userLocation.longitude)
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(initialLatLng, DEFAULT_ZOOM)
                    googleMap.moveCamera(cameraUpdate)
                }
                //for whatever reason the location is not found, move to the default position
                else{
                    val initialLatLng = LatLng(YourDefaultLatitude, YourDefaultLongitude)
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(initialLatLng, DEFAULT_ZOOM)
                    googleMap.moveCamera(cameraUpdate)
                }
            }


        }
    }


    private fun displayImages(latLng: LatLng, data: List<UserData>){
        val nearbyData = ArrayList<UserData>()

            //gets all userData objects that are near (500m) the coords. Adds to object
            for (userData in data) {
                val userDataLatLng = LatLng(userData.latitude, userData.longitude)
                val distance = calculateDistance(latLng, userDataLatLng)
                if (distance <= 0.5){
                    userData.distance = distance
                    nearbyData.add(userData)
                }

            }

        //sort objects by distance. closest displays first..
        nearbyData.sortBy { it.distance }

        //starts the new activity
        if(nearbyData.isNotEmpty())
            startImageActivity(nearbyData)

    }

    private fun startImageActivity(userData: ArrayList<UserData>){
        //starts new activity passes the userData as extra
        val intent = Intent(requireActivity(), ImageActivity::class.java)
        Log.d(TAG,userData.toString())
        intent.putParcelableArrayListExtra("userDataList", userData)
        startActivity(intent)
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
        val heatmapData = ArrayList<LatLng>()
        //for all points in list, add to heatmap data.
        for(d in data){
            heatmapData.add(LatLng(d.latitude,d.longitude))
        }

        //yellow -> Red (custom colors)
        val gradientColors = intArrayOf(
            Color.rgb(238,155,0),
            Color.rgb(174, 32, 18)
        )
        //custom gradient using gradientColors
        val gradient = Gradient(gradientColors, floatArrayOf(0.2f, 1.0f)) // Adjust the gradient as needed

        //setting up the heatmap
        val heatmapTileProvider = HeatmapTileProvider.Builder()
            .data(heatmapData)
            .radius(50)
            .gradient(gradient)
            .build()

        //adding heatmap overlay to the actual map
        val heatmapOverlay = googleMap.addTileOverlay(TileOverlayOptions().tileProvider(heatmapTileProvider))
        startBreathingAnimation(heatmapOverlay!!)

        //dynamicly updating radius based on zoom
        googleMap.setOnCameraIdleListener {
            val currentZoom = googleMap.cameraPosition.zoom
            val newRadius = calculateNewRadius(currentZoom)
            heatmapTileProvider.setRadius(newRadius)
        }

    }


    private fun calculateNewRadius(zoom: Float): Int {
        //heatmap radius helper for dynamic scaling
        val minZoom = 5.0f
        val maxZoom = 15.0f
        val minRadius = 20
        val maxRadius = 50

        val zoomFraction = (zoom - minZoom) / (maxZoom - minZoom)
        return (minRadius + zoomFraction * (maxRadius - minRadius)).toInt()
    }

    private fun startBreathingAnimation(heatmapOverlay: TileOverlay) {
        // Define the animation parameters
        val minAlpha = 0.0f
        val maxAlpha = 0.3f
        val animationDuration = 100L
        val handler = android.os.Handler()
        var increasing = true
        var currentAlpha = minAlpha

        // Create a runnable to update heatmap intensity
        val runnable = object : Runnable {
            override fun run() {
                if (increasing) {
                    currentAlpha += 0.01f
                    if (currentAlpha >= maxAlpha) {
                        currentAlpha = maxAlpha
                        increasing = false
                    }
                } else {
                    currentAlpha -= 0.05f
                    if (currentAlpha <= minAlpha) {
                        currentAlpha = minAlpha
                        increasing = true
                    }
                }

                // Set the new alpha value for heatmap
                heatmapOverlay.setTransparency(currentAlpha)

                // Repeat the animation by posting the runnable
                handler.postDelayed(this, animationDuration)
            }
        }

        // Start the animation
        handler.post(runnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG,"destroyed map fragment")
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG,"paused map fragment")
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val DEFAULT_ZOOM = 15.0f
        private const val YourDefaultLatitude = 47.23890841033145
        private const val YourDefaultLongitude = -68.16202752292156
        private const val TAG = "MapFragment"
    }


}


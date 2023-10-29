package unb.cs2063.hotspots.ui.map

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import unb.cs2063.hotspots.databinding.FragmentMapBinding

class MapFragment : Fragment(), OnMapReadyCallback {
    private val binding get() = _binding!!

    private var _binding: FragmentMapBinding? = null
    private var map: GoogleMap? = null

    private lateinit var currentLocation : Location
    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        //binding
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //maps
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        return root
    }


    override fun onMapReady(googleMap: GoogleMap){
        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        val markerOptions = MarkerOptions().position(latLng).title("current Location")
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,7f))
        map = googleMap
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
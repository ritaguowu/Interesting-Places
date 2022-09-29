package ca.wu.interestingplaces.activities

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import ca.wu.interestingplaces.R
import ca.wu.interestingplaces.models.nearbyplaces.NearbyPlaceModel
import ca.wu.interestingplaces.remote.IGoogleAPIService
import ca.wu.interestingplaces.tools.GeoLocationManager
import ca.wu.interestingplaces.tools.NearbyCommon
import ca.wu.interestingplaces.tools.NearbyCommon.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_map.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


open class MapActivity : AppCompatActivity(), OnMapReadyCallback{

    private lateinit var geoLocationManager: GeoLocationManager
    private var mMap: GoogleMap? = null

    private var mMarker: Marker? = null
    private var latitude: Double = 45.48334
    private var longitude: Double = -73.5801383

    private var defaultLocation = LatLng(latitude, longitude)

    //Nearby places
    private lateinit var mService: IGoogleAPIService
    private var currentPlace: NearbyPlaceModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        setToolBar()

        geoLocationManager = GeoLocationManager(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            geoLocationManager.getLocationPermission()

        geoLocationManager.buildLocationRequest()
        getDeviceLocation()

        //Init Service
        mService = NearbyCommon.googleAPIService

        btn_search.setOnClickListener{
            searchLocation()
        }

        et_search.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                            searchLocation()
                            return true
                        }
                        else -> {}
                    }
                }
                return false
            }
        })

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_hospital -> nearByPlace("hospital")
                R.id.action_market -> nearByPlace("market")
                R.id.action_school -> nearByPlace("school")
                R.id.action_gasStation -> nearByPlace("gasStation")
                R.id.action_restaurant -> nearByPlace("restaurant")
            }
            true
        }
    }

    private fun setToolBar() {
        setSupportActionBar(toolbar_map)

        //Set a back up button in the toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar_map.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Prompt the user for permission.
        geoLocationManager.getLocationPermission()

        mMap!!.uiSettings.isZoomControlsEnabled = true

        // Change to lambda
        mMap!!.setOnMarkerClickListener {
             marker ->
            if (currentPlace!=null) {
                NearbyCommon.currentResult =
                    currentPlace!!.results!![Integer.parseInt(marker.snippet)]
                startActivity(Intent(this@MapActivity, ViewNearbyPlace::class.java))
            }
                true
        }
    }

    private fun nearByPlace(typePlace: String): Boolean {
        //Clear all marker on Map
        mMap!!.clear()

        //Build URL request base on location
        val url = getPlacesUrl(latitude, longitude, typePlace)

        mService.getNearbyPlaces(url)
            .enqueue(object : Callback<NearbyPlaceModel> {
            override fun onResponse(call: Call<NearbyPlaceModel>, response: Response<NearbyPlaceModel>){
                currentPlace = response.body()!!
                if(response.isSuccessful){
                    var latLng: LatLng = defaultLocation
                    for(i in 0 until response.body()!!.results!!.size){
                        val markerOptions = MarkerOptions()
                        val googlePlace = response.body()!!.results!![i]
                        val lat = googlePlace.geometry!!.location!!.lat
                        val lng = googlePlace.geometry!!.location!!.lng
                        val placeName = googlePlace.name
                        latLng = LatLng(lat!!, lng!!)

                        markerOptions.position(latLng)
                        markerOptions.title(placeName)
                        when (typePlace) {
                            "hospital" -> markerOptions.icon(bitmapDescriptorFromVector(this@MapActivity,R.drawable.ic_baseline_local_hospital_24))
                            "school" -> markerOptions.icon(bitmapDescriptorFromVector(this@MapActivity,R.drawable.ic_baseline_school_24))
                            "market" -> markerOptions.icon(bitmapDescriptorFromVector(this@MapActivity,R.drawable.ic_baseline_shopping_cart_24))
                            "restaurant" -> markerOptions.icon(bitmapDescriptorFromVector(this@MapActivity,R.drawable.ic_baseline_restaurant_24))
                            "gasStation" -> markerOptions.icon(bitmapDescriptorFromVector(this@MapActivity,R.drawable.ic_baseline_local_gas_station_24))
                            else -> markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                        }

                        markerOptions.snippet(i.toString())  //Assign index for Market

                        //Add marker to map
                        mMap!!.addMarker(markerOptions)
                    }
                    //Move camera
                    mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                    mMap!!.animateCamera(CameraUpdateFactory.zoomTo(11f))
                }
            }
            override  fun onFailure(call: Call<NearbyPlaceModel>, t: Throwable) {
                Toast.makeText(baseContext, ""+t.message, Toast.LENGTH_SHORT).show()
            }
        })
        return true
    }

    private fun getPlacesUrl(latitude: Double, longitude: Double, typePlace: String): String {
        val googlePlaceUrl = StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
        googlePlaceUrl.append("?keyword=$typePlace&location=$latitude%2C$longitude")
        googlePlaceUrl.append("&radius=10000") //10km
        googlePlaceUrl.append("&type=$typePlace")
        googlePlaceUrl.append("&key=${NearbyCommon.GOOGLE_API_KEY}")

        Log.e("googlePlace", googlePlaceUrl.toString())

        return  googlePlaceUrl.toString()
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            geoLocationManager.getLocationPermission()
            if (geoLocationManager.getLocationPermissionGranted()) {
                val locationResult = geoLocationManager.getFusedLocationClient().lastLocation

                locationResult.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        var deviceLocation = task.result
                        var latLng: LatLng? = null

                        if (deviceLocation != null) {
                            deviceLocation = task.result as Location
                            showOnMap(deviceLocation.latitude, deviceLocation.longitude)
                        }
                        else{
                            showOnMap(NearbyCommon.defaultLocation.latitude, NearbyCommon.defaultLocation.longitude)
                        }
                    }
                }
            }
        }catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun showOnMap(latitude: Double, longitude: Double) {
        val latLng = LatLng(latitude, longitude)
        val markerOptions = MarkerOptions().position(latLng).title("Your position")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        mMarker = mMap!!.addMarker(markerOptions)
        mMarker!!.showInfoWindow()
        mMap!!.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(latitude,longitude), NearbyCommon.DEFAULT_ZOOM.toFloat()
            )
        )
    }


    /**
     * Handles the result of the request for location permissions.
     */

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        geoLocationManager.setLocationPermissionGranted(false)
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    geoLocationManager.setLocationPermissionGranted(true)
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    }



    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }


    private fun searchLocation() {
        mMap!!.clear()
        val location:String = et_search.text.toString().trim()
        var addressList: List<Address>? = ArrayList()
        if (location == ""){
            Toast.makeText(this, "Please provide location", Toast.LENGTH_SHORT).show()
        }else{
            val geoCoder = Geocoder(this)
            try{
                addressList = geoCoder.getFromLocationName(location, 1)
            }catch (e: IOException){
                e.printStackTrace()
            }

            val address = addressList!![0]
            latitude = address.latitude
            longitude = address.longitude
            defaultLocation = LatLng(latitude, longitude)
            mMap!!.addMarker(MarkerOptions().position(defaultLocation).title(location))

            //Zoom in 11 times
            val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(defaultLocation, 11f)
            mMap!!.animateCamera(newLatLngZoom)

        }
    }


}
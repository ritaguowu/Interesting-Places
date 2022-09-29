package ca.wu.interestingplaces.activities


import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ca.wu.interestingplaces.R
import ca.wu.interestingplaces.models.InterestingPlaceModel

import ca.wu.interestingplaces.remote.IGoogleAPIService
import ca.wu.interestingplaces.tools.GeoLocationManager
import ca.wu.interestingplaces.tools.GetPathFromJson
import ca.wu.interestingplaces.tools.NearbyCommon
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng

import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions


import kotlinx.android.synthetic.main.activity_google_map.*
import kotlinx.android.synthetic.main.activity_map.*


import java.io.IOException
import kotlin.math.roundToInt


class GoogleMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var geoLocationManager: GeoLocationManager
    private lateinit var mService: IGoogleAPIService
    private var model: InterestingPlaceModel? = null
    private var mMap: GoogleMap? = null
    private var distance: Float = 0.0f

    private var originLocation: Location? = null
    private var targetLocation: Location? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_map)

        setSupportActionBar(toolbar_google_map)

        //Set a back up button in the toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar_google_map.setNavigationOnClickListener {
            onBackPressed()
        }

        geoLocationManager = GeoLocationManager(this)

        getPlaceLocation()

        //Init Service
        mService = NearbyCommon.googleAPIService


        btn_go.setOnClickListener{
            val address = searchLocation()
            //Calculate distance
            if (address != null) {
                calculateDistance(address)
//                getPathBetweenPlaces()
                getPath()
            }
        }

        et_go.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                            val address = searchLocation()
                            //Calculate distance
                            if (address != null) {
                                calculateDistance(address)
                                getPath()
                            }
                            return true
                        }
                        else -> {}
                    }
                }
                return false
            }
        })


    }

    private fun getPlaceLocation() {
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            model =
                intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as InterestingPlaceModel?
        }
        if (model != null) {
            val mapFragment =
                supportFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment
            mapFragment.getMapAsync(this)
            originLocation = createTargetLocation(model!!.latitude, model!!.longitude)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        geoLocationManager.getLocationPermission()

        mMap!!.uiSettings.isZoomControlsEnabled = true

        showModelLocationOnMap(mMap!!)



    }

    private fun showModelLocationOnMap(googleMap: GoogleMap) {
        //Show the mark on the map. Use ctrl+mouse can zoom in.
        val position = LatLng(model!!.latitude, model!!.longitude)
        val markerOptions = MarkerOptions().position(position).title(model!!.title)
        val originMarker = googleMap.addMarker(markerOptions)
        originMarker!!.showInfoWindow()

        //Zoom in 15 times
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position, 11f)
        googleMap.animateCamera(newLatLngZoom)

    }

    private fun createTargetLocation(latitude: Double, longitude: Double): Location {
        val location = Location("") //provider name is unnecessary
        location.latitude = latitude
        location.longitude = longitude
        return location
    }

    private fun searchLocation(): Address? {
        val location:String = et_go.text.toString().trim()
        var addressList: List<Address>? = ArrayList()
        var address: Address? = null
        if (location == ""){
            Toast.makeText(this, "Please provide location", Toast.LENGTH_SHORT).show()
        }else{
            val geoCoder = Geocoder(this)
            try{
                addressList = geoCoder.getFromLocationName(location, 1)
            }catch (e: IOException){
                e.printStackTrace()
            }
            if (addressList!!.isNotEmpty()) {
                address = addressList[0]
                val latLng = LatLng(address.latitude, address.longitude)
                val markerOptions = MarkerOptions().position(latLng).title(location)
                mMap!!.addMarker(markerOptions)
                mMap!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                val desMarker = mMap!!.addMarker(markerOptions)
                desMarker!!.showInfoWindow()
            }
        }
        return address
    }

    private fun getDirectionsUrl(origin: Location, target: Location): String {
        val origin = "origin=" + origin.latitude + "," + origin.longitude
        val dest = "destination=" + target.latitude + "," + target.longitude
        val params = "$origin&$dest"
        return "https://maps.googleapis.com/maps/api/directions/json?$params&key=${NearbyCommon.GOOGLE_API_KEY}"

    }


    private fun calculateDistance(address: Address) {
        targetLocation = createTargetLocation(address.latitude, address.longitude)
        //Getting URL to the Google Directions API
        distance = originLocation!!.distanceTo(targetLocation)
        distance = (((distance/1000)*100).roundToInt()/100).toFloat()
        Toast.makeText(this, "Distance is $distance KM", Toast.LENGTH_SHORT).show()
    }

    //Step 6: Draw the paths on UI
    private fun getPath() {
        //Google Directions API
        //Note: Directions API should be enabled in google cloud platform
        val url: String = getDirectionsUrl(originLocation!!, targetLocation!!)

        val textTask = GetPathFromJson(url)

        textTask.execute(textTask.SampleCallable(), object : GetPathFromJson.PathListener {
            override fun onPathFound(pathArray: ArrayList<List<LatLng>>) {
                try{
//                  Add  points to polyline and bounds
                    val lineoption = PolylineOptions()
                    for (i in pathArray.indices) {
                        lineoption.addAll(pathArray[i])
                        lineoption.width(5f)
                        lineoption.color(Color.RED)
                        lineoption.geodesic(true)
                    }
                    mMap!!.addPolyline(lineoption)
                } catch (e: Exception) {

                        e.printStackTrace()
                }

            }

            override fun onError() {
                Log.e("Can't get text: ", "Something went wrong")
            }
        })

    }





}
package ca.wu.interestingplaces.tools

import android.Manifest

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

class GeoLocationManager(context: Context) {
    private val context: Context by lazy { context }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationPermissionGranted = false
    private lateinit var locationRequest: LocationRequest


    companion object {
        const val UPDATE_INTERVAL_MILLISECONDS: Long = 5000
        const val FASTEST_UPDATE_INTERVAL_MILLISECONDS = UPDATE_INTERVAL_MILLISECONDS / 2
    }

    init {
        setupLocationProviderClient(context)
    }


    private fun setupLocationProviderClient(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    fun getFusedLocationClient(): FusedLocationProviderClient{
        return fusedLocationClient
    }

    fun getLocationPermissionGranted(): Boolean{
        return locationPermissionGranted
    }
    fun setLocationPermissionGranted(bool: Boolean){
        locationPermissionGranted = bool
    }

    fun buildLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = UPDATE_INTERVAL_MILLISECONDS
            fastestInterval = FASTEST_UPDATE_INTERVAL_MILLISECONDS
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                context as Activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                NearbyCommon.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }


}
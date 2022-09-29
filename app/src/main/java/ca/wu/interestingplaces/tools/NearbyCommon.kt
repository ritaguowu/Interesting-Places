package ca.wu.interestingplaces.tools

import ca.wu.interestingplaces.models.nearbyplaces.ResultModel
import ca.wu.interestingplaces.remote.IGoogleAPIService
import ca.wu.interestingplaces.remote.RetrofitClient
import com.google.android.gms.maps.model.LatLng


//Like static class in java
object NearbyCommon {

    const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    const val DEFAULT_ZOOM = 5

    const val GOOGLE_API_URL = "https://maps.googleapis.com/"
    const val GOOGLE_API_KEY = "AIzaSyA5rZobtiWiTf77P_R97shOxIs2y_4cPlg"

    const val KEY_CAMERA_POSITION = "camera_position"
    const val KEY_LOCATION = "location"

    var defaultLocation = LatLng(45.48334, -73.5801383)


    var currentResult: ResultModel? = null

    val googleAPIService: IGoogleAPIService
        get() = RetrofitClient.getClient(GOOGLE_API_URL).create(IGoogleAPIService::class.java)

    fun setDefaultLocation(latitude: Double, longitude:Double){
        defaultLocation = LatLng(latitude, longitude)
    }


}
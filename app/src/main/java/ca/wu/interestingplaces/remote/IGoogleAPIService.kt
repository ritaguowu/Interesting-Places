package ca.wu.interestingplaces.remote

import ca.wu.interestingplaces.models.directionpath.PathModel
import ca.wu.interestingplaces.models.nearbyplaces.NearbyPlaceModel
import ca.wu.interestingplaces.models.placedetails.PlaceDetail
import ca.wu.interestingplaces.models.placedetails.ResultModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface IGoogleAPIService {
    @GET
    fun getNearbyPlaces(@Url url:String): Call<NearbyPlaceModel>

    @GET
    fun getDetailPlace(@Url url:String): Call<PlaceDetail>

    @GET
    fun getDirectionPath(@Url url:String): Call<PathModel>
}
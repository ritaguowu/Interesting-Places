package ca.wu.interestingplaces.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.util.Log
import android.view.View
import android.widget.Toast
import ca.wu.interestingplaces.R
import ca.wu.interestingplaces.database.DBHelper
import ca.wu.interestingplaces.models.InterestingPlaceModel
import ca.wu.interestingplaces.models.placedetails.PlaceDetail
import ca.wu.interestingplaces.remote.IGoogleAPIService
import ca.wu.interestingplaces.tools.NearbyCommon
import com.squareup.picasso.Picasso

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.android.synthetic.main.activity_view_nearby_place.*
import java.text.SimpleDateFormat
import java.util.*

class ViewNearbyPlace : AppCompatActivity() {

    //internal var will be visible everywhere in the same module
    private lateinit var mService: IGoogleAPIService
    var mPlace: PlaceDetail? = null
    private lateinit var model: InterestingPlaceModel
    private var photoReference: String = ""
    private val maxWidth: Int = 500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_nearby_place)

        //Init Service
        mService = NearbyCommon.googleAPIService


        clearUI()

        //Load photo of place
//        Add implementation 'com.squareup.picasso:picasso:2.5.2' into build.gradle file
        if (NearbyCommon.currentResult!!.photos != null && NearbyCommon.currentResult!!.photos!!.isNotEmpty()) {
            photoReference = NearbyCommon.currentResult!!.photos!![0].photo_reference!!
            Picasso.with(this)!!
                .load(
                    getPhotoOfPlace(
                        NearbyCommon.currentResult!!.photos!![0].photo_reference!!,
                        maxWidth
                    )
                )
                .into(photo)
        }

        //Load Rating
        if (NearbyCommon.currentResult!!.rating != 0.0){
            rating_bar.rating = NearbyCommon.currentResult!!.rating.toFloat()
        }
        else
            rating_bar.visibility = View.GONE

        //Load open hours
        if(NearbyCommon.currentResult!!.opening_hours != null){
            place_open_hour.text = "Open now: " + NearbyCommon.currentResult!!.opening_hours!!.open_now
        }else{
            place_open_hour.visibility = View.GONE
        }

        //Use service to fetch Address and Name

        //Build URL request base on location
        val url = getUrl(NearbyCommon.currentResult!!.place_id!!)
        mService.getDetailPlace(url)
            .enqueue(object :Callback<PlaceDetail>{
                override fun onFailure(call: Call<PlaceDetail>, t: Throwable) {
                    Toast.makeText(baseContext, ""+t.message, Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<PlaceDetail>, response: Response<PlaceDetail>) {
                    mPlace = response.body()

                    place_address.text = mPlace!!.result!!.formatted_address
                    place_name.text = mPlace!!.result!!.name
                    rating_bar.rating = mPlace!!.result!!.rating.toFloat()
                }
            })

        btn_nearby_save.setOnClickListener{
            createModelObject()
            // Initialize the database handler class

            val dbHelper = DBHelper(this)

                val addInterestingPlace =
                    dbHelper.addInterestingPlace(model)
                if (addInterestingPlace > 0) {
                    setResult(Activity.RESULT_OK)
                    Toast.makeText(
                        this,
                        "The interesting place details are inserted successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            finish()
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
        }

        btn_show_map.setOnClickListener{
            createModelObject()
            //Open Map Intent to view
            val intent = Intent(this, GoogleMapActivity::class.java)
            intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, model )
            startActivity(intent)
        }

    }

    private fun createModelObject() {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val date = sdf.format(Calendar.getInstance().time).toString()
        val photoUri = getPhotoUrl(photoReference)


        model = InterestingPlaceModel(
            0,
            mPlace!!.result!!.name,
            photoUri + "photoReference",
            NearbyCommon.currentResult!!.types?.get(0),
            date,
            mPlace!!.result!!.formatted_address,
            NearbyCommon.currentResult!!.geometry!!.location!!.lat!!,
            NearbyCommon.currentResult!!.geometry!!.location!!.lng!!
        )
    }

    private fun getPhotoUrl(photoReference: String): String {
        val googlePlaceUrl = StringBuilder("https://maps.googleapis.com/maps/api/place/photo")
        googlePlaceUrl.append("?maxwidth=$maxWidth")
        googlePlaceUrl.append("&photo_reference=$photoReference")
        googlePlaceUrl.append("&key=${NearbyCommon.GOOGLE_API_KEY}")

        Log.e("googlePlace", googlePlaceUrl.toString())

        return  googlePlaceUrl.toString()
    }

    private fun getUrl(place_id: String): String {
        val googlePlaceUrl = StringBuilder("https://maps.googleapis.com/maps/api/place/details/json")
        googlePlaceUrl.append("?fields=name%2Crating%2Cformatted_address")
        googlePlaceUrl.append("&place_id=$place_id")
        googlePlaceUrl.append("&key=${NearbyCommon.GOOGLE_API_KEY}")

        Log.e("googlePlace", googlePlaceUrl.toString())

        return  googlePlaceUrl.toString()
    }

    private fun getPhotoOfPlace(photoReference: String, maxWidth: Int): String {
        val googlePlaceUrl = StringBuilder("https://maps.googleapis.com/maps/api/place/photo")
        googlePlaceUrl.append("?maxwidth=$maxWidth")
        googlePlaceUrl.append(" &photo_reference=$photoReference")
        googlePlaceUrl.append("&key=${NearbyCommon.GOOGLE_API_KEY}")

        Log.e("googlePlace", googlePlaceUrl.toString())

        return  googlePlaceUrl.toString()
    }


    private fun clearUI() {
        place_name.text = ""
        place_address.text = ""
        place_open_hour.text = ""
    }
}


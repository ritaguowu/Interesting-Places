package ca.wu.interestingplaces.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ca.wu.interestingplaces.R
import ca.wu.interestingplaces.models.InterestingPlaceModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_add_interesting_place.iv_place_image
import kotlinx.android.synthetic.main.activity_interesting_place_detail.*

class InterestingPlaceDetailActivity : AppCompatActivity() {

    private var model: InterestingPlaceModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interesting_place_detail)

        loadModel()
    }

    private fun loadModel() {
        model = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as InterestingPlaceModel?

        if (model != null){
            //Set the navigation tool bar
            setSupportActionBar(toolbar_place_detail)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = model!!.title

            toolbar_place_detail.setNavigationOnClickListener{
                onBackPressed()
            }

            //Load image
            var imageString:String? = model?.image
            if (imageString?.contains("photoReference") == true){
                imageString = model?.image!!.removeSuffix("photoReference")
                Picasso.with(this)
                    .load(imageString)
                    .into(iv_place_image);
            }else {
                iv_place_image.setImageURI(Uri.parse(model?.image))
            }


            tv_description.text = model?.address
            tv_location.text = model?.location

            btn_view_on_map.setOnClickListener{
                val intent = Intent(this, GoogleMapActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, model )
                startActivity(intent)
            }
        }
    }


}
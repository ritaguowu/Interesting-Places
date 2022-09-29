package ca.wu.interestingplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ca.wu.interestingplaces.R
import ca.wu.interestingplaces.activities.AddInterestingPlaceActivity
import ca.wu.interestingplaces.activities.MainActivity
import ca.wu.interestingplaces.database.DBHelper
import ca.wu.interestingplaces.models.InterestingPlaceModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_interesting_place.view.*

//It means Open classes and methods in Kotlin are equivalent to the opposite of final in Java, an open method is overridable and an open class is extendable in Kotlin.
open class PlacesAdapter(private val context: Context, private var list: ArrayList<InterestingPlaceModel>)
    :RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    //Step 2
    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_interesting_place, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val place = list[position]

        if(holder is MyViewHolder) {
            //Load image
            var imageString:String? = place.image
            if (imageString?.contains("photoReference") == true){
                imageString = place.image!!.removeSuffix("photoReference")
                Picasso.with(context)
                    .load(imageString)
                    .into(holder.itemView.cv_place_image);
            }else {
                holder.itemView.cv_place_image.setImageURI(Uri.parse(place.image))
            }
            holder.itemView.tvTitle.text = place.title
            holder.itemView.tvDescription.text = place.address
        }

        holder.itemView.setOnClickListener{
            if(onClickListener != null){
                onClickListener!!.onClick(position, place)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }



    /**
     * A function to edit the added happy place detail and pass the existing details through intent.
     */
    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int) {
        val intent = Intent(context, AddInterestingPlaceActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])
        activity.startActivityForResult(
            intent,
            requestCode
        ) // Activity is started with requestCode

        notifyItemChanged(position)// Notify any registered observers that the item at position has changed.
    }

    fun notifyDeleteItem(position: Int) {
        val dbHelper = DBHelper(context)
        val isDelete = dbHelper.deleteInterestingPlace(list[position])
        if (isDelete >0) {
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    //Step 1: Strategy design pattern
    interface OnClickListener{
        fun onClick(position: Int, model: InterestingPlaceModel)
    }

    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view){}

}
package ca.wu.interestingplaces.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.wu.interestingplaces.R
import ca.wu.interestingplaces.adapters.PlacesAdapter
import ca.wu.interestingplaces.database.DBHelper
import ca.wu.interestingplaces.models.InterestingPlaceModel
import ca.wu.interestingplaces.tools.SwipeToDeleteCallback
import ca.wu.interestingplaces.tools.SwipeToEditCallback
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Need to add the plugin: "id 'kotlin-android-extensions' in the Module bulid.gradle file
        //Then without view binding, we can use the view element directly
        fabAddInterestingPlace?.setOnClickListener {
            val intent = Intent(this, AddInterestingPlaceActivity::class.java)

            //Difference between startActivity and startActivityForResult
            //startActivity can only jump to another activity, but when adding an interesting place
            //and come back to the main activity, the list page won't update with the content of arraylist.
            //So we have to use startActivityForResult for notifying the view needs to update according to
            // the content of the arraylist
//            startActivity(intent)
            startActivityForResult(intent, REQUEST_CODE)
        }
        getInterestingPlacesListFromSQLitDB()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // check if the request code is same as what is passed  here it is 'REQUEST_CODE'
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                getInterestingPlacesListFromSQLitDB()
            } else {
                Log.e("Activity", "Cancelled or Back Pressed")
            }
        }
    }

    private fun getInterestingPlacesListFromSQLitDB(){
        val dbHelper = DBHelper(this)
//        dbHandler.deleteAllRecordsFromDB()
        val getInterestingPlaceList = dbHelper.getAllPlaces()

        if(getInterestingPlaceList.size >0){
            rv_interesting_places_list.visibility = View.VISIBLE
            tv_no_records.visibility = View.GONE
            setupInterestingPlacesRecyclerView(getInterestingPlaceList)
        }else{
            rv_interesting_places_list.visibility = View.GONE
            tv_no_records.visibility = View.VISIBLE
        }

    }


    private fun setupInterestingPlacesRecyclerView(interestingPlaceList: ArrayList<InterestingPlaceModel>){
        rv_interesting_places_list.layoutManager = LinearLayoutManager(this)
        rv_interesting_places_list.setHasFixedSize(true)

        val adapter = PlacesAdapter(this,interestingPlaceList)
        rv_interesting_places_list.adapter = adapter

        adapter.setOnClickListener(object :
            PlacesAdapter.OnClickListener {
            override fun onClick(position: Int, model: InterestingPlaceModel) {
                val intent = Intent(this@MainActivity, InterestingPlaceDetailActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model) // Passing the complete serializable data class to the detail activity using intent.
                startActivity(intent)
            }
        })

        //Step 3
        //Edit an item
        val editSwipeHandler = object : SwipeToEditCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val newAdapter = rv_interesting_places_list.adapter as PlacesAdapter
                newAdapter.notifyEditItem(this@MainActivity, viewHolder.adapterPosition, REQUEST_CODE)
            }
        }

        //Make the edit item touch work
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(rv_interesting_places_list)


        //Delete swipe
        val deleteSwipeHandler = object : SwipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val newAdapter = rv_interesting_places_list.adapter as PlacesAdapter
                newAdapter.notifyDeleteItem(viewHolder.adapterPosition)

                getInterestingPlacesListFromSQLitDB()
            }
        }

        //Make the delete item touch work
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(rv_interesting_places_list)

    }


    //Just like Java static property
    companion object{
        val EXTRA_PLACE_DETAILS = "extra_place_details"
        val REQUEST_CODE = 1
    }
}
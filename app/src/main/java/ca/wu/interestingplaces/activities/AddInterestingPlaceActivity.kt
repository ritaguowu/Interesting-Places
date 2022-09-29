package ca.wu.interestingplaces.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.*

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import ca.wu.interestingplaces.R
import ca.wu.interestingplaces.database.DBHelper
import ca.wu.interestingplaces.models.InterestingPlaceModel
import ca.wu.interestingplaces.tools.GetAddressFromLatLng
import ca.wu.interestingplaces.tools.NearbyCommon
import com.google.android.gms.location.*

import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_add_interesting_place.*
import kotlinx.android.synthetic.main.activity_add_interesting_place.iv_place_image
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception

import java.text.SimpleDateFormat
import java.util.*

class AddInterestingPlaceActivity : AppCompatActivity(), View.OnClickListener {
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListListener: DatePickerDialog.OnDateSetListener

    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    private var model: InterestingPlaceModel? = null

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val contentURI = data.data
                    if (contentURI != null) {
                        val selectedImageBitmap = getCapturedImage(contentURI)
                        saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)

                        Log.e("Saved image:", "Path:: $saveImageToInternalStorage")

                        iv_place_image.setImageBitmap(selectedImageBitmap)
                    }
                }
            }
        }
    val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val thumbnail: Bitmap = data.extras!!.get("data") as Bitmap
                    saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)

                    Log.e("Saved image:", "Path:: $saveImageToInternalStorage")
                    iv_place_image.setImageBitmap(thumbnail)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_interesting_place)

        //Need to add the plugin: "id 'kotlin-android-extensions' in the Module build.gradle file
        setSupportActionBar(toolbar_add_place)

        //Set a back up button in the toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar_add_place.setNavigationOnClickListener {
            onBackPressed()
        }

        //LocationServices: The main entry point for location services integration.
        //FusedLocationProviderClient: The main entry point for interacting with the fused location provider.
        //It's recommended to use Google Play services version 11.6.0 or higher(before we used GoogleApiClient)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        model = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as InterestingPlaceModel?
        if (model != null) {
            supportActionBar?.title = "Edit INTERESTING PLACE"
            et_title.setText(model!!.title)
            et_description.setText(model!!.address)
            et_location.setText(model!!.location)
            et_date.setText(model!!.date)
            mLatitude = model!!.latitude
            mLongitude = model!!.longitude
            saveImageToInternalStorage = Uri.parse(model!!.image)
            iv_place_image.setImageURI(saveImageToInternalStorage)
            btn_save.text = "UPDATE"
            tv_add_image.text = "EDIT IMAGE"
        }

        dateSetListListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        et_date.setOnClickListener(this)
        tv_add_image.setOnClickListener(this)
        btn_save.setOnClickListener(this)
        et_location.setOnClickListener(this)
        tv_current_location.setOnClickListener(this)
    }

    private fun isLocationEnabled():Boolean{
        //LocationManager class provides access to the system location services.
        // These services allow applications to obtain periodic updates of the device's geographical
        // location, or to be notified when the device enters the proximity of a given geographical location.
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                ||locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun createLocationRequest(){
        //LocationRequest objects are used to request a quality of service for location
        // updates from the FusedLocationProviderApi.
        //
        //For example, if your application wants high accuracy location it should create a
        // location request with setPriority(int) set to PRIORITY_HIGH_ACCURACY and
        // setInterval(long) to 5 seconds. This would be appropriate for mapping applications
        // that are showing your location in real-time.
        val mLocationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 50
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//            numUpdates = 1
        }
        //Looper class used to run a message loop for a thread.
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallBack, Looper.myLooper())
    }

    private val mLocationCallBack = object: LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            mLatitude = mLastLocation.latitude
            Log.i("Current Latitude: ", "$mLatitude")
            mLongitude = mLastLocation.longitude
            Log.i("Current Longitude: ", "$mLongitude")

            NearbyCommon.setDefaultLocation(mLatitude, mLongitude)

            val addressTask = GetAddressFromLatLng(this@AddInterestingPlaceActivity, mLatitude, mLongitude)

            addressTask.execute(addressTask.SampleCallable(), object : GetAddressFromLatLng.AddressListener{
                override fun onAddressFound(address: String) {
                    et_location.setText(address)
                }
                override fun onError() {
                    Log.e("Get Address: ", "Something went wrong")
                }
            })
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.et_date -> {
                DatePickerDialog(
                    this@AddInterestingPlaceActivity,
                    dateSetListListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            R.id.tv_add_image -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems =
                    arrayOf("Select photo from Gallery", "Capture photo from camera")
                pictureDialog.setItems(pictureDialogItems) { _, which ->
                    when (which) {
                        0 -> choosePhoto("GALLERY")
                        1 -> choosePhoto("CAMERA")
                    }
                }
                pictureDialog.show()
            }
            R.id.et_location ->{
                try{
                    val googleIntent = Intent(this, MapActivity::class.java)
                    startActivity(googleIntent)

                }catch (e: Exception){
                    e.printStackTrace()
                }

            }
            R.id.tv_current_location ->{
                if(!isLocationEnabled()){
                    Toast.makeText(this, "Your location provider is turned off. Please turn it on", Toast.LENGTH_SHORT).show()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }else{
                    Dexter.withContext(this@AddInterestingPlaceActivity).withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ).withListener(object: MultiplePermissionsListener{
                         override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if (report!!.areAllPermissionsGranted()){
                                createLocationRequest()
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            p0: MutableList<PermissionRequest>?,
                            p1: PermissionToken?
                        ) {
                            showRationalDialogForPermissions()
                        }
                    }).onSameThread().check()
                }
            }
            R.id.btn_save -> {
                //Save the DataModel into database
                when {
                    et_title.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show()
                    }
                    et_description.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT).show()
                    }

                    saveImageToInternalStorage == null -> {
                        Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        if (et_date.text.isNullOrEmpty()) {
                            updateDateInView()
                        }
                        val interestingPlaceModel = InterestingPlaceModel(
                            if (model == null) 0 else model!!.id,
                            et_title.text.toString(),
                            saveImageToInternalStorage.toString(),
                            et_description.text.toString(),
                            et_date.text.toString(),
                            et_location.text.toString(),
                            mLatitude,
                            mLongitude
                        )
                        // Initialize the database handler class

                        val dbHelper = DBHelper(this)

                        if (model == null) {
                            val addInterestingPlace =
                                dbHelper.addInterestingPlace(interestingPlaceModel)
                            if (addInterestingPlace > 0) {
                                setResult(Activity.RESULT_OK)
                                Toast.makeText(
                                    this,
                                    "The interesting place details are inserted successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                //for testing
//                            et_title.setText("")
//                            et_title.requestFocus()
                            }
                        } else {
                            //Edit
                            val editInterestingPlace =
                                dbHelper.updateInterestingPlace(interestingPlaceModel)
                            if (editInterestingPlace > 0) {
                                Toast.makeText(
                                    this,
                                    "The interesting place details are updated successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                setResult(Activity.RESULT_OK)
                            }
                        }
                        finish()
                    }
                }
            }
        }
    }


    private fun choosePhoto(type: String) {
        //Step 1: Need permission for use Galley and Camera
        //Add uses-permission into the Manifest.xml file

        // below line is use to request permission in the current activity.
        Dexter.withContext(this@AddInterestingPlaceActivity)
            // below line is use to request the number of permissions which are required in our app.
            .withPermissions(
                // after adding permissions we are calling an with listener method.
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
//                Manifest.permission.ACCESS_MEDIA_LOCATION,
            ).withListener(
                object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {

                            if (type == "GALLERY") {
                                val galleryIntent =
                                    Intent(
                                        Intent.ACTION_PICK,
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                    )
                                galleryLauncher.launch(galleryIntent)
                            }
                            if (type == "CAMERA") {
                                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                cameraLauncher.launch(cameraIntent)
                            }

                        }
                        if (report.isAnyPermissionPermanentlyDenied) {
                            // permission is denied permanently,
                            // we will show user a dialog message.
                            showRationalDialogForPermissions()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>,
                        token: PermissionToken
                    ) {
                        token.continuePermissionRequest()
                    }
                    // below line is use to run the permissions on same thread and to check the permissions
                }).onSameThread().check()
    }

    private fun getCapturedImage(selectedPhotoUri: Uri): Bitmap {
        val bitmap = when {
            Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(
                this.contentResolver,
                selectedPhotoUri
            )
            else -> {
                val source = ImageDecoder.createSource(this.contentResolver, selectedPhotoUri)
                ImageDecoder.decodeBitmap(source)
            }
        }
        return bitmap
    }

    private fun showRationalDialogForPermissions() {
        android.app.AlertDialog.Builder(this).setTitle("Need Permissions")
            .setMessage("This app needs permission to use this feature. You can grant them in app settings.")
            .setPositiveButton("GOTO SETTINGS") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun updateDateInView() {
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        et_date.setText(sdf.format(cal.time).toString())
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(application)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.parse(file.absoluteFile.toString())
    }

    companion object {
        private const val IMAGE_DIRECTORY = "InterestingPlacesImages"
    }
}
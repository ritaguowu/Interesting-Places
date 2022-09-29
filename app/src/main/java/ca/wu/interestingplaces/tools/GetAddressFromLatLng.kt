package ca.wu.interestingplaces.tools

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Handler
import android.os.Looper
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors

//AsyncTask is designed to be a helper class around Thread and Handler and does
// not constitute a generic threading framework. AsyncTasks should ideally be used
// for short operations (a few seconds at the most.) If you need to keep threads
// running for long periods of time, it is highly recommended you use the various
// APIs provided by the java.util.concurrent package such as Executor,
// ThreadPoolExecutor and FutureTask.
//
// https://tedblob.com/asynctask-deprecated-alternative-android-kotlin/

class GetAddressFromLatLng(context:Context, private val latitude: Double,
                           private val longitude: Double){

    // create a new instance of Executor using any factory methods
    private val executor: Executor = Executors.newSingleThreadExecutor()
    // handler of UI thread
    private val handler = Handler(Looper.getMainLooper())

    //Geocoding is the process of transforming a street address or other description
    //of a location into a (latitude, longitude) coordinate. Reverse geocoding is the
    //process of transforming a (latitude, longitude) coordinate into a (partial) address.
    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())

    interface AddressListener {
        fun onAddressFound(address: String)
        fun onError()
    }

    // callable to communicate the result back to UI
    fun execute(callable: SampleCallable, callback: AddressListener) {
        executor.execute {
            val address: String?
            try {
                // execute the callable or any tasks asynchronously
                address = callable.call()
                handler.post {
                    // update the result back to UI
                    callback.onAddressFound(address)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                handler.post { // communicate error or handle
                    callback.onError()
                }
            }
        }
    }
    //SampleCallable: Find the address by geocoder.getFromLocation()
    inner class SampleCallable : Callable<String> {
        override fun call(): String {
                val addressList: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

                if (addressList != null && addressList.isNotEmpty()) {
                    val address: Address = addressList[0]
                    val sb = StringBuilder()
                    for (i in 0..address.maxAddressLineIndex) {
                        sb.append(address.getAddressLine(i)).append(" ")
                    }
                    //Delete the last empty space
                    sb.deleteCharAt(sb.length - 1)

                    return sb.toString()
                }
            return ""
        }
    }
}
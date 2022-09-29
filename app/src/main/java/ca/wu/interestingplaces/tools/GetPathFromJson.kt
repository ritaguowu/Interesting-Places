package ca.wu.interestingplaces.tools

import android.os.Handler
import android.os.Looper
import ca.wu.interestingplaces.models.directionpath.PathModel
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import java.net.URL
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class GetPathFromJson(private val url: String) {

    // create a new instance of Executor using any factory methods
    private val executor: Executor = Executors.newSingleThreadExecutor()

    // handler of UI thread
    private val handler = Handler(Looper.getMainLooper())

    interface PathListener {
        fun onPathFound(pathArray: ArrayList<List<LatLng>>)
        fun onError()
    }

    // callable to communicate the result back to UI
    fun execute(callable: SampleCallable, callback: PathListener) {
        executor.execute {
            val pathArray = ArrayList<List<LatLng>>()
            try {
                // execute the callable or any tasks asynchronously
                val data = callable.call()
                val respObj = Gson().fromJson(data, PathModel::class.java)
                val path = ArrayList<LatLng>()


                for (i in 0 until respObj!!.routes?.get(0)!!.legs?.get(0)!!.steps!!.size) {

                    path.addAll(
                        decodePoly(
                            respObj!!.routes?.get(0)!!.legs?.get(0)!!.steps?.get(
                                i
                            )!!.polyline!!.points!!
                        )
                    )
                }
                pathArray.add(path)
                handler.post {
                    // update the result back to UI
                    callback.onPathFound(pathArray)
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
            return URL(url).readText()
        }
    }


    /**
     * Method to decode polyline points
     * Courtesy : https://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private fun decodePoly(encoded: String): ArrayList<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(lat.toDouble() / 1E5,
                lng.toDouble() / 1E5)
            poly.add(p)
        }

        return poly
    }
}

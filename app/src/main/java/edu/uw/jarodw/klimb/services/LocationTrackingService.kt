// Credit to Gentra Aditya Putra Ruswanda on GitHub
package edu.uw.jarodw.klimb

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class LocationTrackingService : Service() {

    val context = this
    var locationManager: LocationManager? = null

    init {
        instance = this
    }

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onCreate() {
        if (locationManager == null)
            locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, INTERVAL, DISTANCE, locationListeners[1])
        } catch (e: SecurityException) {
            Log.e(TAG, "Fail to request location update", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Network provider does not exist", e)
        }

        try {
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, DISTANCE, locationListeners[0])
        } catch (e: SecurityException) {
            Log.e(TAG, "Fail to request location update", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "GPS provider does not exist", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (locationManager != null)
            for (i in 0..locationListeners.size) {
                try {
                    locationManager?.removeUpdates(locationListeners[i])
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to remove location listeners")
                }
            }
    }

    fun sendLocation(lat: Double, long: Double) {
        var params = hashMapOf<String, Any>()
        params["lat"] = lat
        params["long"] = long
        params["user_id"] = getSharedPreferences("klimb-preferences", 0).getInt("userId", -1)
        System.out.println(params["user_id"])
        val jsonPayload = JSONObject(params as Map<Any, Any>)
        val createLocationRequest = JsonObjectRequest(Request.Method.POST, "https://klimb-backend.herokuapp.com/send-location", jsonPayload, Response.Listener<JSONObject> {
                response ->
            var jsonObject: JSONObject = JSONObject(response.toString())
            var message = jsonObject.getString("message")
            if (message.equals("Updated Location")) {
                Toast.makeText(applicationContext(), "Location has been Sent", Toast.LENGTH_LONG).show()

            } else {
                Toast.makeText(applicationContext(), "Location has not been sent", Toast.LENGTH_LONG).show()
            }

        }, Response.ErrorListener {
            Toast.makeText(applicationContext(), "Error with sending location", Toast.LENGTH_LONG).show()
        })

        val queue= Volley.newRequestQueue(applicationContext())
        queue.add(createLocationRequest)
    }


    companion object {
        val TAG = "LocationTrackingServices"

        val INTERVAL = 1000.toLong() // In milliseconds
        val DISTANCE = 10.toFloat() // In meters

        private var instance: LocationTrackingService? = null
        val locationListeners = arrayOf(
            LTRLocationListener(LocationManager.GPS_PROVIDER),
            LTRLocationListener(LocationManager.NETWORK_PROVIDER)
        )

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }

        fun sharedPreferences(): SharedPreferences {
            return instance!!.getSharedPreferences("klimb-preferences", 0)
        }

        class LTRLocationListener(provider: String) : android.location.LocationListener {

            val lastLocation = Location(provider)

            override fun onLocationChanged(location: Location?) {
                lastLocation.set(location)
                println("======== User ID: ${sharedPreferences().getInt("userId", 0)} ========")
            }

            override fun onProviderDisabled(provider: String?) {
            }

            override fun onProviderEnabled(provider: String?) {
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

        }
    }

}
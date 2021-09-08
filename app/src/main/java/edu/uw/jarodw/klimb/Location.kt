package edu.uw.jarodw.klimb

import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import org.json.JSONObject

class Location : AppCompatActivity(), OnMapReadyCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }

        fun createLocationRequest() {
            val locationRequest = LocationRequest.create()?.apply {
                interval = 10000
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest!!)
        }

        val builder = LocationSettingsRequest.Builder()
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this@Location, 0x1)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }




        val mapFragment = SupportMapFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        mapFragment.getMapAsync(this)
        transaction.add(R.id.MapTest, mapFragment, "Player Location")
        transaction.addToBackStack(null)
        transaction.commit()

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val pos = LatLng(it.latitude, it.longitude)
                System.out.println("Lat= " + it.latitude )
                System.out.println("Long= " + it.longitude )
                googleMap?.addMarker(MarkerOptions().position(pos).title("Player"))


                var params = hashMapOf<String, Any>()
                params["lat"] = it.latitude.toFloat()
                params["long"] = it.longitude.toFloat()
                params["user_id"] = getSharedPreferences("klimb-preferences", 0).getInt("userId", -1)

                val jsonPayload = JSONObject(params as Map<Any, Any>)
                val createLocationRequest = JsonObjectRequest(Request.Method.POST, "https://klimb-backend.herokuapp.com/send-location", jsonPayload,
                    Response.Listener<JSONObject> { response ->
                    var jsonObject: JSONObject = JSONObject(response.toString())
                        var message = jsonObject.getString("message")
                    if (message.equals("Updated Location")) {
                        Toast.makeText(applicationContext, "Location has been Sent", Toast.LENGTH_LONG).show()

                    } else {
                        Toast.makeText(applicationContext, "Location has not been sent", Toast.LENGTH_LONG).show()
                    }

                }, Response.ErrorListener {
                    Toast.makeText(applicationContext, "Error with sending location", Toast.LENGTH_LONG).show()
                    System.out.println(it.networkResponse)
                })

                val queue= Volley.newRequestQueue(applicationContext)
                queue.add(createLocationRequest)
            }
        }
    }

}
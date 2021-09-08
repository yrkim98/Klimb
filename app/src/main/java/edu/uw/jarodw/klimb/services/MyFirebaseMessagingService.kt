package edu.uw.jarodw.klimb.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import edu.uw.jarodw.klimb.MainActivity
import edu.uw.jarodw.klimb.R
import edu.uw.jarodw.klimb.VolleyService
import org.json.JSONException
import org.json.JSONObject
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val ADMIN_CHANNEL_ID = "admin_channel"
    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val requestQueue = VolleyService.getInstance(this).requestQueue

    // executes when the app first launches because that is when the firebase token is generated
    // saves the token as a shared preference
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        val sharedPref = getSharedPreferences("klimb-preferences", 0)
        val editor = sharedPref.edit()
        editor.putString("firebaseToken", p0)
        editor.commit()
    }

    // Handles incoming firebase notifications
    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        val intent = Intent(this, MainActivity::class.java)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt(3000)

        /*
        Apps targeting SDK 26 or above (Android O) must implement notification channels and add its notifications
        to at least one of them.
      */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels(notificationManager)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val largeIcon = BitmapFactory.decodeResource(
            resources,
            R.drawable.ic_launcher_background
        )

        val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setLargeIcon(largeIcon)
            .setContentTitle(p0?.data?.get("title"))
            .setContentText(p0?.data?.get("message"))
            .setAutoCancel(true)
            .setSound(notificationSoundUri)
            .setContentIntent(pendingIntent)

        //Set notification color to match your app color template
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.color = resources.getColor(R.color.colorPrimary)
        }
        notificationManager.notify(notificationID, notificationBuilder.build())
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupChannels(notificationManager: NotificationManager?) {
        val adminChannelName = "New notification"
        val adminChannelDescription = "Device to devie notification"

        val adminChannel: NotificationChannel
        adminChannel = NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH)
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        notificationManager?.createNotificationChannel(adminChannel)
    }

    // creates the json of a notification that is targeting a specific device
    fun createTargetedNotification(title: String, message: String, targetDevice: String): JSONObject {
        val notification = JSONObject()
        val notifcationBody = JSONObject()

        try {
            notifcationBody.put("title", title)
            notifcationBody.put("message", message)   //Enter your notification message
            notification.put("to", targetDevice)
            notification.put("data", notifcationBody)
            Log.e("TAG", "try")
        } catch (e: JSONException) {
            Log.e("TAG", "onCreate: " + e.message)
        }
        return notification
    }

    // sends the notification through Firebase
    fun sendNotification(notification: JSONObject) {
        Log.e("TAG", "sendNotification")
        val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
            Response.Listener<JSONObject> { response ->
                Log.i("TAG", "onResponse: $response")
            },
            Response.ErrorListener {
                Toast.makeText(this, "Request error", Toast.LENGTH_LONG).show()
                Log.i("TAG", "onErrorResponse: Didn't work")
            }) {

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = "key=" + R.string.firebaseServerKey.toString()
                params["Content-Type"] = "application/json"
                return params
            }
        }
        requestQueue.add(jsonObjectRequest)
    }
}
package edu.uw.jarodw.klimb

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        var sharedPrefs = getSharedPreferences("klimb-preferences", Context.MODE_PRIVATE)
        var editor = sharedPrefs.edit()
        editor.putInt("userId", 123456789)
        editor.commit()
        fab.setOnClickListener {
            val intent = Intent(applicationContext, Location::class.java)
            startActivity(intent)
        }
        startService(Intent(this, LocationTrackingService::class.java))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.items, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        super.onOptionsItemSelected(item)
        if (item.itemId == R.id.create) {
            startActivity(Intent(applicationContext, CreateGame::class.java))
        } else if (item.itemId == R.id.join) {
            startActivity(Intent(applicationContext, JoinGame::class.java))

        } else if (item.itemId == R.id.profile) {
            startActivity(Intent(applicationContext, Profile::class.java))
        } else if (item.itemId == R.id.view_my_games){
            startActivity(Intent(applicationContext, Game::class.java))
        }
        return true
    }

    override fun onDestroy() {
        stopService(Intent(this, LocationTrackingService::class.java))
        super.onDestroy()
    }

    fun checkPermissions() {
        // Read SMS permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1)}
        // Receive SMS permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                1)}
        // Send SMS permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                1)}
        // Read Contact permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS),
                1)}
    }
}

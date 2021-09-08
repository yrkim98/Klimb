package edu.uw.jarodw.klimb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_register.*

class Profile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val sharedPref = getSharedPreferences("klimb-preferences", 0)
        val username = sharedPref.getString("username", "USERNAME")
        val email = sharedPref.getString("email", "EMAIL")
        val phone = sharedPref.getString("phone", "XXXXXXXXXX")

        usernameValue.text = username
        emailValue.text = email
        phoneValue.text = phone

    }
}

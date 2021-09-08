package edu.uw.jarodw.klimb

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import edu.uw.jarodw.klimb.R
import edu.uw.jarodw.klimb.ui.login.LoginActivity
import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONObject
import java.lang.Error

class Register : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        backButton.setOnClickListener{
            startActivity(
                Intent(applicationContext, LoginActivity::class.java)
            )
        }

        submitButton.setOnClickListener {

            try {
                if (usernameTextField.text.toString() == "" || usernameTextField.text.toString() == null || phoneTextField.text.toString() == "" || phoneTextField.text.toString() == null || emailTextField.text.toString() == "" || emailTextField.text.toString() == null || passwordTextField.text.toString() == "" || passwordTextField.text.toString() == null || repeatPasswordTextField.text.toString() == "" || repeatPasswordTextField.text.toString() == null) {
                    Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
                }


                Log.e("TAG", passwordTextField.text.toString())
                Log.e("TAG", repeatPasswordTextField.text.toString())

                if (passwordTextField.text.toString().equals(repeatPasswordTextField.text.toString())) {
                    val username = usernameTextField.text.toString()
                    val email = emailTextField.text.toString()
                    val phone = phoneTextField.text.toString()
                    val hashedPassword = passwordTextField.text.toString()
                    val queue = Volley.newRequestQueue(this)
                    val params = hashMapOf<String, String>()
                    params["username"] = username
                    params["email"] = email
                    params["phone_number"] = phone
                    params["password"] = hashedPassword
                    val jsonPayload = JSONObject(params as Map<Any, Any>)
                    val stringRequest = JsonObjectRequest(Request.Method.POST, "https://klimb-backend.herokuapp.com/register", jsonPayload,
                        Response.Listener<JSONObject> {response ->
                            var jsonObject: JSONObject = JSONObject(response.toString())
                            var userId = jsonObject.getString("userId")

                            val sharedPref = getSharedPreferences("klimb-preferences", Context.MODE_PRIVATE)
                            val editor = sharedPref.edit()
                            editor.putBoolean("authenticated", true)
                            editor.putInt("userId", userId.toInt())
                            editor.putString("username", username)
                            editor.putString("phone", phone)
                            editor.putString("email", email)
                            editor.commit()
                        }, Response.ErrorListener { print("didn\'t work") })
                    queue.add(stringRequest)
                    startActivity(
                        Intent(applicationContext, MainActivity::class.java)
                    )
                } else {
                    Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Error) {
                Toast.makeText(this, "We\'re sorry, something is wrong. We\'ll get back to you.", android.widget.Toast.LENGTH_SHORT).show()
                print(e)
            }
        }

    }
}

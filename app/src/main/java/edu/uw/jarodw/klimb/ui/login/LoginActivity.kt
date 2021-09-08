package edu.uw.jarodw.klimb.ui.login

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import edu.uw.jarodw.klimb.MainActivity

import edu.uw.jarodw.klimb.R
import edu.uw.jarodw.klimb.Register
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        isUserLoggedIn()

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val login = findViewById<Button>(R.id.login)
        val loading = findViewById<ProgressBar>(R.id.loading)

        loginViewModel = ViewModelProviders.of(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }


        }

        login.setOnClickListener {
//            loading.visibility = View.VISIBLE
            Log.e("tag", username.text.toString())
            Log.e("tag", password.text.toString())
            try {
                var loginStatus = false
                val queue = Volley.newRequestQueue(this)
                val params = hashMapOf<String, String>()
                params["username"] = username.text.toString()
                params["password"] = password.text.toString()
                val jsonPayload = JSONObject(params as Map<Any, Any>)
                val stringRequest = JsonObjectRequest(Request.Method.POST, "https://klimb-backend.herokuapp.com/login", jsonPayload,
                    Response.Listener<JSONObject> { response ->
                        var jsonObject: JSONObject = JSONObject(response.toString())
                        var message = jsonObject.getString("message")
                        Log.e("tag", message)
                        if (message.equals("Successfully logged in")) {
                            Log.e("innertag", message)
                            loginStatus = true
                            var jsonObject: JSONObject = JSONObject(response.toString())
                            var userId = jsonObject.getString("userId")
                            val username = jsonObject.getString("username")
                            val phone = jsonObject.getString("phone")
                            val email = jsonObject.getString("email")
                            val sharedPref = getSharedPreferences("klimb-preferences", 0)
                            val editor = sharedPref.edit()
                            editor.putBoolean("authenticated", true)
                            editor.putInt("userId", userId.toInt())
                            editor.putString("username", username)
                            editor.putString("phone", phone)
                            editor.putString("email", email)
                            editor.commit()
                            startActivity(
                                Intent(applicationContext, MainActivity::class.java)
                            )
                        }

                    }, Response.ErrorListener { print("Something is wrong") })
                queue.add(stringRequest)
//                if (loginStatus) {
//                    Log.e("login", loginStatus.toString())
//                    startActivity(
//                        Intent(applicationContext, MainActivity::class.java)
//                    )
//                }
                if (!loginStatus) {
                    Toast.makeText(this, "Bad password", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Error) {
                print(e)
            }

        }


        createAccountButton.setOnClickListener {
            startActivity(
                Intent(applicationContext, Register::class.java)
            )
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    private fun isUserLoggedIn() {
        val getter = getSharedPreferences("klimb-preferences", 0)
        val authstatus = getter.getBoolean("authenticated", true)
        if (authstatus) {
            startActivity(
                Intent(applicationContext, MainActivity::class.java)
            )
        }
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}

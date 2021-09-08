package edu.uw.jarodw.klimb

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlin.random.Random
import kotlinx.android.synthetic.main.activity_create_game.*
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class CreateGame : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_game)
        val gameCode = generateCode()
        game_code.setText(gameCode)
        createGame.setOnClickListener {
            createGame(gameCode)
        }

    }
    fun generateCode():String {
        var randomCode: String = ""
        for (i in 0..13) {
            if (i == 4 || i == 9) {
                randomCode +="-"
            } else if (Random.nextBoolean()) {
                randomCode += (0 until 10).random()
            } else {
                randomCode += ('A' until 'Z').random()
            }


        }
        return randomCode;
    }

    fun createGame(code: String) {
        val sharedPref = getSharedPreferences("klimb-preferences", 0)
        var params = hashMapOf<String, Any>()
        // Two elements always there
        params["join_code"] = code
        params["user_id"] = sharedPref.getInt("userId", -1)
        if (name.getText().toString() == "") {
            Toast.makeText(applicationContext, "No Game Name Provided", Toast.LENGTH_LONG).show()
        } else {
            params["name"] = name.getText().toString()

        }
        if (max_time.getText().toString() == "") {
            Toast.makeText(applicationContext, "Need to provide max time for game!", Toast.LENGTH_LONG).show()
        } else {
            params["max_hours"] = max_time.getText().toString().toInt()
        }
        if (max_players.getText().toString() == "") {
            Toast.makeText(applicationContext, "Need to provide max players for game!", Toast.LENGTH_LONG).show()
        } else {
            params["max_players"] = max_players.getText().toString().toInt()

        }

        // continue if none missing
        if (params.size ==5) {
            val jsonPayload = JSONObject(params as Map<Any, Any>)
            val createGameRequest = JsonObjectRequest(Request.Method.POST, "https://klimb-backend.herokuapp.com/create-game", jsonPayload, Response.Listener<JSONObject> {
                    response ->
                var jsonObject: JSONObject = JSONObject(response.toString())
                var message = jsonObject.getString("message")
                if (message.equals("Game Created")) {
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    Toast.makeText(applicationContext, "Sucessfully Created Game", Toast.LENGTH_LONG).show()

                } else {
                    Toast.makeText(applicationContext, "Some Error while creating game.", Toast.LENGTH_LONG).show()
                }

            }, Response.ErrorListener {
                Toast.makeText(applicationContext, "something wrong in create game", Toast.LENGTH_LONG).show()
            })
            val queue= Volley.newRequestQueue(applicationContext)
            queue.add(createGameRequest)

        }

    }
}

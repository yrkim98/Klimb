package edu.uw.jarodw.klimb

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_join_game.*
import org.json.JSONObject

class JoinGame : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_game)
        join_game.setOnClickListener {
            if (!(join_code.getText().toString() == null || join_code.getText().toString() == "")) {
                sendJoinRequest(join_code.getText().toString())
            } else {
                Toast.makeText(applicationContext, "Need To Specify Game-ID to join.", Toast.LENGTH_LONG).show()
            }
        }
    }
    fun sendJoinRequest(code: String) {
        var params = hashMapOf<String, Any>()
        params["join_code"] = code
        params["user_id"] = getSharedPreferences("klimb-preferences", 0).getInt("userId", -1)
        val jsonPayload = JSONObject(params as Map<Any, Any>)
        val createGameRequest = JsonObjectRequest(Request.Method.POST, "https://klimb-backend.herokuapp.com/join-game", jsonPayload, Response.Listener<JSONObject> {
                response ->
            var jsonObject: JSONObject = JSONObject(response.toString())
            var message = jsonObject.getString("message")
            if (message.equals("Joined game")) {
                Toast.makeText(applicationContext, "Sucessfully Joined", Toast.LENGTH_LONG).show()

            } else {
                Toast.makeText(applicationContext, "Invalid Game ID.", Toast.LENGTH_LONG).show()
            }

        }, Response.ErrorListener {
            Toast.makeText(applicationContext, "something wrong in create game", Toast.LENGTH_LONG).show()
        })

        val queue= Volley.newRequestQueue(applicationContext)
        queue.add(createGameRequest)
    }


}

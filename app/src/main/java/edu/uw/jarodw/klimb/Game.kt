package edu.uw.jarodw.klimb

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_game.*
import org.json.JSONArray
import org.json.JSONObject


class Game: AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        val gameRecyclerView= game_recycler_view
        gameRecyclerView.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        val sharedPref = getSharedPreferences("klimb-preferences", 0)
        val req = "https://klimb-backend.herokuapp.com/games/user/" +  sharedPref.getInt("userId", -1).toString()
        val createGameRequest = JsonArrayRequest(Request.Method.POST, req , null, Response.Listener<JSONArray> {
                response ->
            var gameList = mutableListOf<Game2>()
            for (i in 1 until response.length()) {
                val jsonObj = response.getJSONObject(i)
                gameList.add(Game2(jsonObj.getInt("game_id"), jsonObj.getString("name"),
                    jsonObj.getString("join_code"), 10,
                    1, 1))


            }
            gameRecyclerView.adapter = GameAdapter(gameList, applicationContext)
            gameRecyclerView.adapter!!.notifyDataSetChanged()

        }, Response.ErrorListener {
            Toast.makeText(applicationContext, "something wrong in create game", Toast.LENGTH_LONG).show()
        })
        val queue= Volley.newRequestQueue(applicationContext)
        queue.add(createGameRequest)

    }


}

data class Game2(val gameId: Int, val name:String, val joinCode:String, val max_time: Int, val currentKlimber: Int, val gameCreator:Int)

class GameAdapter(private val gameList: List<Game2>, private val context: Context):
    RecyclerView.Adapter<GameAdapter.GameHolder>() {

    override fun onBindViewHolder(holder: GameHolder, position: Int) {
         holder.name.text = gameList[position].name
        holder.maxTime.text = "Maximum Time: " + gameList[position].max_time
        holder.gameCreator.text = "Join Code: " + gameList[position].joinCode

        var onClickListener = View.OnClickListener {
            val inte = Intent(context, Leaderboards::class.java).apply {
                putExtra("gameId", gameList[position].gameId)
                putExtra("name", gameList[position].name)
                putExtra("joinCode", gameList[position].joinCode)
                putExtra("max_time", gameList[position].max_time)
                putExtra("current", gameList[position].currentKlimber)
                putExtra("creator", gameList[position].gameCreator)


            }
            startActivity(context, inte.setFlags(FLAG_ACTIVITY_NEW_TASK), null)
        }
        with(holder.itemView) {
            setOnClickListener(onClickListener)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType:Int) : GameHolder {
        return GameHolder(LayoutInflater.from(context).inflate(R.layout.game_single, parent, false))
    }
    override fun getItemCount(): Int {
        return gameList.size
    }



    inner class GameHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name:TextView = view.findViewById(R.id.game_name)
        val maxTime:TextView = view.findViewById(R.id.max_time)
        val gameCreator:TextView = view.findViewById(R.id.game_creator)
    }


}
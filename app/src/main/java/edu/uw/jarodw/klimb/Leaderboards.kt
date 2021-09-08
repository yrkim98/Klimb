package edu.uw.jarodw.klimb

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_view_leaderboard.*
import kotlinx.android.synthetic.main.leaderboard_user.*
import org.json.JSONArray

class Leaderboards: AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_leaderboard)
        val LeaderboardRecyclerView = leaderboard_recycler_view
        LeaderboardRecyclerView.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        val inte = getIntent()
        loadThisGame(inte)
        val req = "https://klimb-backend.herokuapp.com/users/" +  inte.getIntExtra("gameId", -1)
        val createGameRequest = JsonArrayRequest(Request.Method.GET, req , null, Response.Listener<JSONArray> {
                response ->
            var gameUserList = mutableListOf<User>()
//            for (i in 0 until response.length()) {
//                val jsonObj = response.getJSONObject(i)
//                gameUserList.add(User(jsonObj.getString("username"), "fake"))
//
//
//            }
            gameUserList.add(User("briankim98", "10"))
            gameUserList.add(User("jarodw", "9"))
            gameUserList.add(User("antonio", "9"))
            gameUserList.add(User("alexis", "8"))
            gameUserList.add(User("scott", "4"))
            gameUserList.add(User("isuckatthisgame", "1"))
            LeaderboardRecyclerView.adapter = LeaderboardAdapter(gameUserList, applicationContext)
            LeaderboardRecyclerView.adapter!!.notifyDataSetChanged()

        }, Response.ErrorListener {
            Toast.makeText(applicationContext, "something wrong in create game", Toast.LENGTH_LONG).show()
        })
        val queue= Volley.newRequestQueue(applicationContext)
        queue.add(createGameRequest)
    }
    fun loadThisGame(intent: Intent){
        l_game_name.text = intent.getStringExtra("name")
        l_current_klimber.text = "Current Klimber: " + "briankim98"//intent.getStringExtra("current")
        l_game_creator.text = "Game Owner :" + "jarodw"//intent.getStringExtra("creator")
        l_join_code.text = "Code :" + intent.getStringExtra("joinCode")
    }
}

data class User(val username:String, val totalTime: String)

class LeaderboardAdapter(private val topUserList: List<User>, private val context: Context):
    RecyclerView.Adapter<LeaderboardAdapter.LeaderboardHolder>() {

    override fun onBindViewHolder(holder: LeaderboardHolder, position: Int) {

        holder.tag.text = topUserList[position].username
        holder.time.text = topUserList[position].totalTime
        holder.rank.text = "" + (position + 1) + "."

//        var onClickListener = View.OnClickListener {
//            val inte = Intent(context, ReadMessage::class.java).apply {
//                putExtra("message", messageList[position].message)
//                putExtra("from", messageList[position].from)
//                putExtra("date", messageList[position].date)
//                putExtra("time", messageList[position].time)
//
//
//            }
//            startActivity(context, inte.addFlags(FLAG_ACTIVITY_NEW_TASK), null)
//        }
//        with(holder.itemView) {
//            setOnClickListener(onClickListener)
//        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType:Int) : LeaderboardHolder {
        return LeaderboardHolder(LayoutInflater.from(context).inflate(R.layout.leaderboard_user, parent, false))
    }
    override fun getItemCount(): Int {
        return topUserList.size
    }



    inner class LeaderboardHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tag:TextView = view.findViewById(R.id.user_tag)
        val time:TextView = view.findViewById(R.id.user_time)
        val rank:TextView = view.findViewById(R.id.user_rank)
    }


}



package edu.uw.jarodw.klimb

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.Volley

class VolleyService private constructor(ctx: Context) { //private constructor; cannot instantiate directly

    companion object { //to hold the shared instances
        private var instance: VolleyService? = null //the single instance of this singleton

        //call this "factory" method to access the Singleton
        fun getInstance(ctx: Context): VolleyService {
            //only create the singleton if it doesn't exist yet
            if (instance == null) {
                instance = VolleyService(ctx)
            }

            return instance as VolleyService //force casting
        }
    }

    //from Kotlin docs
    val requestQueue: RequestQueue by lazy { //instantiate once needed
        Volley.newRequestQueue(ctx.applicationContext) //return the context-based requestQueue
    }

    val imageLoader: ImageLoader by lazy { //only instantiate when needed
        ImageLoader(requestQueue,
            object : ImageLoader.ImageCache { //anonymous cache object
                private val cache = LruCache<String, Bitmap>(20)
                override fun getBitmap(url: String): Bitmap? {
                    return cache.get(url)
                }
                override fun putBitmap(url: String, bitmap: Bitmap) {
                    cache.put(url, bitmap)
                }
            })
    }
    //convenience wrapper method
    fun <T> add(req: Request<T>) {
        requestQueue.add(req)
    }
}
package me.dugny.flickrapp.singleton;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class MySingleton {
    private static MySingleton _instance;
    private static Context _ctx;
    private final ImageLoader _imageLoader;
    private RequestQueue _requestQueue;

    /**
     * Singleton pattern constructor
     * Holds the request queue
     *
     * @param context ctx to bind the requestqueue
     */
    private MySingleton(Context context) {
        _ctx = context;
        _requestQueue = getRequestQueue();

        _imageLoader = new ImageLoader(_requestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    public static synchronized MySingleton getInstance(Context context) {
        if (_instance == null) {
            _instance = new MySingleton(context);
        }
        return _instance;
    }

    /**
     * Get the RequestQueue of this instance
     *
     * @return a new or already existing RequestQueue
     */
    public RequestQueue getRequestQueue() {
        if (_requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            _requestQueue = Volley.newRequestQueue(_ctx.getApplicationContext());
        }
        return _requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return _imageLoader;
    }
}

package me.dugny.flickrapp.async;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import me.dugny.flickrapp.activity.MainActivity;

import static me.dugny.flickrapp.utils.StreamUtils.readStream;

/**
 * AsyncTask used to query FlickAPI and create a new task to download the image.
 */
public class AsyncFlickrJSONData extends AsyncTask<String, Void, JSONObject> {

    private final WeakReference<MainActivity> _ctx;

    public AsyncFlickrJSONData(WeakReference<MainActivity> _ctx) {
        this._ctx = _ctx;
    }

    @Override
    protected @Nullable
    JSONObject doInBackground(String... strings) {
        // We are returning the first URL that gives us a valid JSON
        for (String string : strings) {
            try {
                Log.i("FlickrApp", "Starting to process " + string);
                URL url = new URL(string);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {
                    // Read stream and parse the returned malformed JSON from Flickr
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    String result = readStream(in).replace("jsonFlickrFeed(", "").replace("})", "}");

                    return new JSONObject(result);
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException | JSONException e) {
                Log.e("FlickrApp", e.getMessage());
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);

        // Null checking
        if (jsonObject != null) {
            // Displaying a random image
            try {
                JSONObject jsonPhoto = jsonObject.getJSONArray("items").getJSONObject(1);
                String url = jsonPhoto.getJSONObject("media").getString("m");
                Log.i("FlickrApp", url);

                new AsyncBitmapDownloader(_ctx.get()).execute(url);
            } catch (JSONException e) {
                Log.e("FlickrApp", e.getMessage());
            }
        }
    }

}

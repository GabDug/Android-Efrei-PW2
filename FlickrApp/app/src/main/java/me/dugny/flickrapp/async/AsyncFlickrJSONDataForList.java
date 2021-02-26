package me.dugny.flickrapp.async;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import me.dugny.flickrapp.adapter.MyAdapter;

import static me.dugny.flickrapp.utils.StreamUtils.readStream;

/**
 * AsyncTask used to query FlickAPI and fill the adapter of images
 */
public class AsyncFlickrJSONDataForList extends AsyncTask<String, Void, JSONObject> {
    private MyAdapter _adapter;

    public AsyncFlickrJSONDataForList(MyAdapter adapter) {
        this._adapter = adapter;
    }

    @Override
    protected @Nullable
    JSONObject doInBackground(String... strings) {
        // Return the first URL that gives us a valid JSON
        for (String string : strings) {
            try {
                Log.i("FlickrApp", "Starting to process " + string);
                URL url = new URL(string);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {
                    // Read stream and parse the returned malformed JSON from Flickr
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    // Remove the leading and trailing stuff from the JSON payload
                    String result = readStream(in).replace("jsonFlickrApi(", "").replace("})", "}");

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

        // We got a valid JSON
        if (jsonObject != null) {
            // Logging it
            Log.i("FlickrApp", jsonObject.toString());

            // Displaying a random image
            try {
                JSONArray jsonPhotos = jsonObject.getJSONObject("photos").getJSONArray("photo");

                for (int i = 0; i < jsonPhotos.length(); i++) {
                    // Querying data from returned JSON
                    JSONObject jsonPhoto = jsonPhotos.getJSONObject(i);

                    // Build the URL
                    String url = String.format("https://live.staticflickr.com/%s/%s_%s_b.jpg", jsonPhoto.getString("server"), jsonPhoto.getString("id"), jsonPhoto.getString("secret"));

                    // Updating the adaptter
                    Log.i("FlickrApp", "Adding to adapter url: " + url);
                    this._adapter.add(url);
                }

                // Mark the adapter as changed
                this._adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                Log.e("FlickrApp", e.getMessage());
            }
        }
    }
}

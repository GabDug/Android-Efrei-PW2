package me.dugny.flickrapp.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import me.dugny.flickrapp.R;
import me.dugny.flickrapp.adapter.MyAdapter;
import me.dugny.flickrapp.async.AsyncFlickrJSONDataForList;

/**
 * Activity used to display a List of images
 */
public class ListActivity extends AppCompatActivity {
    // Services references
    private LocationManager _locationManager;

    // Layout references
    private ListView _listView;

    // Instantiate the adapter backing the list
    private MyAdapter _myAdapter = new MyAdapter(this);

    // Async references
    private final AsyncFlickrJSONDataForList _asyncTask = new AsyncFlickrJSONDataForList(this._myAdapter);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // Recover references
        this._listView = findViewById(R.id.list);

        // Set adapter
        this._listView.setAdapter(this._myAdapter);

        // Get keyword from Settings
        String keyword = PreferenceManager.getDefaultSharedPreferences(this).getString("keyword", "");

        // Get a LocationManager
        this._locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Get last known GPS position
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = this._locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        Log.i("FlickrApp", String.format("Location: %s %s", latitude, longitude));

        // Load data
        String url = String.format("https://api.flickr.com/services/rest/?method=flickr.photos.search&license=4&api_key=%s&tags=%s&per_page=50&format=json", PreferenceManager.getDefaultSharedPreferences(this).getString("flickrAPI", "5c110e7aaec0ef3aef2e578673900648"), keyword);
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("location", false)) {
            url += String.format("&has_geo=1&lat=%s&lon=%s", latitude, longitude);
        }
        // Launch and forget
        this._asyncTask.execute(url);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Properly cancel the async task
        if (this._asyncTask != null) {
            this._asyncTask.cancel(true);
        }
    }
}
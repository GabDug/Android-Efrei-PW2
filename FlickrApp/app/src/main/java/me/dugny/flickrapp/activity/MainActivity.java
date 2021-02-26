package me.dugny.flickrapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import me.dugny.flickrapp.R;
import me.dugny.flickrapp.async.AsyncFlickrJSONData;

/**
 * Default Activity used to query a random image through a Button
 */
public class MainActivity extends AppCompatActivity {
    // Click handler
    private final GetImageOnClickListener _getImageOnClickListener = new GetImageOnClickListener(this);
    // Layout elements references
    private Button _getImageBtn;
    private Button _goListActivity;
    private Button _goPreferenceActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Recover references
        this._getImageBtn = findViewById(R.id.getImageBtn);
        this._goListActivity = findViewById(R.id.goListActivity);
        this._goPreferenceActivity = findViewById(R.id.goPreferenceActivity);

        // Set events
        this._getImageBtn.setOnClickListener(this._getImageOnClickListener);
        this._goListActivity.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListActivity.class);
            startActivity(intent);
        });
        this._goPreferenceActivity.setOnClickListener(v -> {
            Intent intent = new Intent(this, PreferenceActivity.class);
            startActivity(intent);
        });

        // Set the default preferences if the user don't go to the preference screen
        PreferenceManager.setDefaultValues(this, R.xml.preference, false);

        // Verify we have access to GPS data
        // Verify we have the permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            // Ask for permissions
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i("FlickrApp", String.format("requestCode: %d", requestCode));
        Log.i("FlickrApp", String.format("permissions: %s", Arrays.toString(permissions)));
        Log.i("FlickrApp", String.format("grantResults: %s", Arrays.toString(grantResults)));

        if (requestCode == 1) {
            if (grantResults.length == 0 || (grantResults[0] == PackageManager.PERMISSION_DENIED && grantResults[1] == PackageManager.PERMISSION_DENIED)) {
                // No permissions closing the app
                Toast.makeText(this, R.string.toast_error_gps_permission, Toast.LENGTH_SHORT).show();

                finishAffinity();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Destroy the task on exit
        this._getImageOnClickListener.cancel();
    }

    /**
     * onClickListener related to getting a random image from FlickrAPI
     */
    protected static class GetImageOnClickListener implements View.OnClickListener {
        private final MainActivity _ctx;
        private AsyncFlickrJSONData _asyncTask = null;

        public GetImageOnClickListener(MainActivity ctx) {
            this._ctx = ctx;
        }


        @Override
        public void onClick(View v) {
            this._asyncTask = new AsyncFlickrJSONData(new WeakReference<>(_ctx));

            // Get keyword from Settings
            String keyword = PreferenceManager.getDefaultSharedPreferences(_ctx).getString("keyword", "");

            // Launch and forget
            this._asyncTask.execute("https://www.flickr.com/services/feeds/photos_public.gne?tags=" + keyword + "&format=json");
        }

        /**
         * Cancel the task
         */
        public void cancel() {
            if (this._asyncTask != null) {
                this._asyncTask.cancel(true);
            }
        }
    }
}
package me.dugny.flickrapp.async;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import me.dugny.flickrapp.R;
import me.dugny.flickrapp.activity.MainActivity;

/**
 * Override the default AsyncBitmapDownloader to handle this activity use case without losing modularity
 */
public class AsyncBitmapDownloader extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<MainActivity> _ctx;

    public AsyncBitmapDownloader(MainActivity ctx) {
        this._ctx = new WeakReference<>(ctx);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        // Default behaviour
        super.onPostExecute(bitmap);

        // Check we do have a bitmap
        if (bitmap != null) {
            // Recover reference or rtn
            MainActivity ctx = _ctx.get();
            if (ctx == null) return;

            ImageView imageView = ctx.findViewById(R.id.imageView);
            imageView.setImageBitmap(bitmap);
        }
    }

    @Override
    protected @Nullable
    Bitmap doInBackground(String... strings) {
        for (String string : strings) {
            try {
                URL url = new URL(string);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                return BitmapFactory.decodeStream(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
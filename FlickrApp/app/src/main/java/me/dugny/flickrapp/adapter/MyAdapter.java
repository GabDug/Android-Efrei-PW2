package me.dugny.flickrapp.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.preference.PreferenceManager;

import com.android.volley.toolbox.ImageRequest;

import java.util.Vector;

import me.dugny.flickrapp.R;
import me.dugny.flickrapp.singleton.MySingleton;

/**
 * Adapter used to fill a list of pictures
 */
public class MyAdapter extends BaseAdapter {
    private final Activity _ctx;
    private final Vector<String> _urls = new Vector<>();

    public MyAdapter(Activity ctx) {
        this._ctx = ctx;
    }

    /**
     * Add a String to the list of URLs
     *
     * @param toAdd URL to add
     */
    public void add(String toAdd) {
        this._urls.add(toAdd);
    }

    @Override
    public int getCount() {
        return _urls.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = (ImageView) convertView;

        // Inflate if the view doesn't exist
        if (imageView == null) {
            imageView = (ImageView) _ctx.getLayoutInflater().inflate(R.layout.bitmap_layout, parent, false);
        }

        // Prepare the ImageRequest
        ImageRequest imageRequest = new ImageRequest(_urls.get(position), imageView::setImageBitmap, 0, 0, ImageView.ScaleType.FIT_XY, Bitmap.Config.RGB_565, e -> {
            Log.e("FlickrApp", "Error while loading images");
        });

        // Enable or not the caching
        imageRequest.setShouldCache(PreferenceManager.getDefaultSharedPreferences(_ctx).getBoolean("cache", true));

        // Add it to the queue
        MySingleton.getInstance(parent.getContext()).addToRequestQueue(imageRequest);

        return imageView;
    }


    @Override
    public Object getItem(int position) {
        Log.i("FlickrApp", "TODO getItem");
        return null;
    }

    @Override
    public long getItemId(int position) {
        Log.i("FlickrApp", "TODO getItemId");
        return 0;
    }
}
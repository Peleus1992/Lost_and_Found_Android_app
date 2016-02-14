package edu.gatech.wguo64.lostandfoundandroidapp.utility;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by guoweidong on 2/12/16.
 */
public class ImageDownloader extends AsyncTask<String, Void, Drawable> {

    ImageView imageView;
    public ImageDownloader(ImageView imageView) {
        this.imageView = imageView;
    }

    @Override
    protected Drawable doInBackground(String... params) {

        try {
            URL url = new URL(params[0]);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.connect();
            Drawable drawable = BitmapDrawable.createFromStream(connection.getInputStream(), params[0]);
            return drawable;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Drawable drawable) {
        super.onPostExecute(drawable);
        imageView.setImageDrawable(drawable);
    }
}

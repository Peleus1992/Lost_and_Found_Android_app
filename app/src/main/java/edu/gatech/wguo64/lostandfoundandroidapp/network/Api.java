package edu.gatech.wguo64.lostandfoundandroidapp.network;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.MyApi;


/**
 * Created by mkatri on 11/22/15.
 */
public class Api {
    private static MyApi myApi = null;

    public static void initialize(GoogleAccountCredential credential) {
        MyApi.Builder builder = new MyApi.Builder
                (AndroidHttp.newCompatibleTransport(), new
                        AndroidJsonFactory(), credential)
                .setRootUrl("https://lost-and-found-android-app.appspot.com/_ah/api/");
        builder.setApplicationName("Lost & Found Android App");
        myApi = builder.build();
    }

    public static MyApi getClient() {
        return myApi;
    }
}

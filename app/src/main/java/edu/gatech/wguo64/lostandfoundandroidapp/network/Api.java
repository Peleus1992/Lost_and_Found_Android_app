package edu.gatech.wguo64.lostandfoundandroidapp.network;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import edu.gatech.cc.lostandfound.api.lostAndFound.LostAndFound;

/**
 * Created by mkatri on 11/22/15.
 */
public class Api {
    private static LostAndFound client = null;

    public static void initialize(GoogleAccountCredential credential) {
        LostAndFound.Builder builder = new LostAndFound.Builder
                (AndroidHttp.newCompatibleTransport(), new
                        AndroidJsonFactory(), credential)
                .setRootUrl("https://lost-and-found-android-app.appspot.com/_ah/api/");
        builder.setApplicationName("Lost & Found Android App");
        client = builder.build();
    }

    public static LostAndFound getClient() {
        return client;
    }
}

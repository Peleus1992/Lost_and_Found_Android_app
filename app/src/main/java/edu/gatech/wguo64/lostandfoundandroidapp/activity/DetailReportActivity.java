package edu.gatech.wguo64.lostandfoundandroidapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.plus.PlusShare;

import java.io.IOException;


import edu.gatech.wguo64.lostandfoundandroidapp.R;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.GeoPt;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.Report;
import edu.gatech.wguo64.lostandfoundandroidapp.googlemaps.LocationHelper;
import edu.gatech.wguo64.lostandfoundandroidapp.network.Api;
import edu.gatech.wguo64.lostandfoundandroidapp.utility.ImageDownloader;
import edu.gatech.wguo64.lostandfoundandroidapp.utility.TimeConvertor;

public class DetailReportActivity extends AppCompatActivity implements View.OnClickListener{
    public final static String TAG = DetailReportActivity.class.getName();

    public Toolbar toolbar;

    public ImageView userPhotoImg;
    public TextView titleTxt;
    public TextView userEmailTxt;
    public TextView timestampTxt;
    public TextView descriptionTxt;
    public ImageView objectImage;
    public TextView statusTxt;
    public TextView tagTxt;
    public TextView datetimeTxt;
    public MapFragment mapFragment;
    public Button emailBtn;
    public Button commentBtn;
    public Button shareBtn;

    GoogleMap googleMap;

    Report report;

    public ProgressBar progressBar;

    public SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_report);
        //Preferences
        preferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        // Get LostReport
        Intent intent = getIntent();
        Long reportId = intent.getLongExtra("reportId", -1);
        if(reportId == -1) {
            Log.d(TAG, "no reportId");
            finish();
        }

        inflateViews();
        new ReportDownloader().execute(reportId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.emailBtn:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", (String)v.getTag(), null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
                break;
            case R.id.commentBtn:{
                Intent intent = new Intent(this, CommentActivity.class);
                intent.putExtra("reportId", report.getId());
                startActivity(intent);
                break;
            }
            case R.id.shareBtn: {
                PlusShare.Builder builder = new PlusShare.Builder(this);

                // Set call-to-action metadata.
                builder.addCallToAction(
                        "CREATE_ITEM", /** call-to-action button label */
                        Uri.parse("http://plus.google.com/pages/create"), /** call-to-action url (for desktop use) */
                        "/pages/create" /** call to action deep-link ID (for mobile use), 512 characters or fewer */);

                // Set the content url (for desktop use).
                builder.setContentUrl(Uri.parse("https://plus.google.com/pages/"));

                // Set the target deep-link ID (for mobile use).
                builder.setContentDeepLinkId("/pages/",
                        null, null, null);

                // Set the share text.
                String text = "Report by " + report.getUserEmail() + "\r\n" + "\r\n"
                        + "Report type: " + (report.getReportType() ? "Lost report" : "Found report")
                        + "Title: " + report.getTitle() + "\r\n" + "\r\n"
                        + "Description: " + report.getDescription() + "\r\n" + "\r\n"
                        + "When: " + (report.getTime() == null ? "Not clear." : report.getTime()) + "\r\n" + "\r\n"
                        + "Where: " + (report.getLocation() == null ? "Not clear." : LocationHelper.getAddress(this, new LatLng(report.getLocation().getLatitude()
                        , report.getLocation().getLongitude()))) + "\r\n" + "\r\n"
                        + "Photo url: " + report.getImageUrl()+ "\r\n";
                builder.setText(text);
                this.startActivityForResult(builder.getIntent(), 0);
                break;
            }
        }
    }

    class ReportDownloader extends AsyncTask<Long, Void, Report> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Report doInBackground(Long... params) {
            try {
                return Api.getClient().report().get(params[0]).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Report foundReport) {
            super.onPostExecute(foundReport);
            if(foundReport == null) {
                finish();
            }
            report = foundReport;
            setUI();
            progressBar.setVisibility(View.GONE);
        }
    }

    private void inflateViews() {
        userPhotoImg = (ImageView) findViewById(R.id.userPhotoImg);
        titleTxt = (TextView) findViewById(R.id.titleTxt);
        userEmailTxt = (TextView) findViewById(R.id.userEmailTxt);
        timestampTxt = (TextView) findViewById(R.id.timestampTxt);
        descriptionTxt = (TextView) findViewById(R.id.descriptionTxt);
        objectImage = (ImageView) findViewById(R.id.objectImage);
        statusTxt = (TextView) findViewById(R.id.statusTxt);
        tagTxt = (TextView) findViewById(R.id.tagTxt);
        datetimeTxt = (TextView) findViewById(R.id.datetimeTxt);
        mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        emailBtn = (Button) findViewById(R.id.emailBtn);
        commentBtn = (Button) findViewById(R.id.commentBtn);
        shareBtn = (Button) findViewById(R.id.shareBtn);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
    }

    private void setUI() {
        //Toolbar
        toolbar = (Toolbar)findViewById(R.id.toolBar);
        toolbar.setTitle(report.getReportType() ? "Detailed Lost Report" : "Detailed Found Report");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DetailReportActivity.this.onBackPressed();
            }
        });
        //User photo
        new ImageDownloader(userPhotoImg).execute(report.getPhotoUrl());
        //Title
        titleTxt.setText(report.getTitle());
        //User Email
        userEmailTxt.setText(report.getUserEmail());
        //Timestamp
        timestampTxt.setText(TimeConvertor.getTimeDifferential(report.getCreated().getValue()));
        //Description
        descriptionTxt.setText(report.getDescription());
        //Object Image
        if(report.getImageUrl() != null) {
            new ImageDownloader(objectImage).execute(report.getImageUrl());
        }
        //Status
        statusTxt.setText(report.getReportType() ? (report.getStatus() ? "Found" : "Not Found")
                : (report.getStatus() ? "Returned" : "Not Returned"));
        statusTxt.setTextColor(report.getStatus() ? Color.GREEN : Color.RED);
        //Tags
        if(report.getTags() != null) {
            for (String t : report.getTags()) {
                tagTxt.append(t + "/");
            }
        }
        //Datetime
        datetimeTxt.setText(report.getTime() == null ? "I could not remember."
                : TimeConvertor.getDateTime(report.getTime().getValue()));
        //Email button
        emailBtn.setOnClickListener(this);
        emailBtn.setTag(report.getUserEmail());
        //Comment button
        commentBtn.setOnClickListener(this);
        //Share button
        shareBtn.setOnClickListener(this);
        //Position
        googleMap = mapFragment.getMap();

        // check if map is created successfully or not
        if (googleMap == null) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                    .show();
        } else {
            if(checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_GRANTED
                    || checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
            }
            markLocation(report.getLocation());
        }

        //Progressbar
        progressBar.setVisibility(View.GONE);
    }

    private void markLocation(GeoPt geoPt) {
        if(geoPt != null){
            // Getting latitude of the current location
            double latitude = geoPt.getLatitude();

            // Getting longitude of the current location
            double longitude = geoPt.getLongitude();

            // Creating a LatLng object for the current location
            LatLng latLng = new LatLng(latitude, longitude);

            // Showing the current location in Google Map
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            // Zoom in the Google Map
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

            String markerTitle = LocationHelper.getAddress(this, latLng);
            markerTitle = "".equals(markerTitle) ? "Latitude: " + latitude
                    + ", Longitude: " + longitude : markerTitle;
            MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude))
                    .title(markerTitle);

            // adding marker
            googleMap.addMarker(marker);
        } else {
            Log.d(TAG, "No such location");
        }
    }

}

package edu.gatech.wguo64.lostandfoundandroidapp.activity;

import android.content.Intent;
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

import java.io.IOException;


import edu.gatech.wguo64.lostandfoundandroidapp.R;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.GeoPt;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.LostReport;
import edu.gatech.wguo64.lostandfoundandroidapp.network.Api;
import edu.gatech.wguo64.lostandfoundandroidapp.utility.ImageDownloader;
import edu.gatech.wguo64.lostandfoundandroidapp.utility.TextTrimmer;
import edu.gatech.wguo64.lostandfoundandroidapp.utility.TimeConvertor;

public class DetailLostActivity extends AppCompatActivity implements View.OnClickListener {
    public final static String TAG = DetailLostActivity.class.getName();

    public Toolbar toolbar;

    public ImageView userPhotoImg;
    public TextView titleTxt;
    public TextView timestampTxt;
    public TextView descriptionTxt;
    public TextView statusTxt;
    public TextView datetimeTxt;
    public MapFragment mapFragment;
    public Button emailBtn;
    public Button commentBtn;
    public Button shareBtn;

    GoogleMap googleMap;

    LostReport report;

    public ProgressBar progressBar;

    public final static String TITLE = "Detailed Lost Report";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_lost);
        // Get LostReport
        Intent intent = getIntent();
        Long reportId = intent.getLongExtra("reportId", -1);
        if(reportId == -1) {
            Log.d(TAG, "no reportId");
            finish();
        }
        new ReportDownloader().execute(reportId);
        inflateViews();
        setUI();
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
            case R.id.commentBtn:
                break;
            case R.id.shareBtn:
                break;
        }
    }

    class ReportDownloader extends AsyncTask<Long, Void, LostReport> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected LostReport doInBackground(Long... params) {
            try {
                return Api.getClient().lostReport().get(params[0]).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(LostReport lostReport) {
            super.onPostExecute(lostReport);
            report = lostReport;
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void inflateViews() {
        userPhotoImg = (ImageView) findViewById(R.id.userPhotoImg);
        titleTxt = (TextView) findViewById(R.id.titleTxt);
        timestampTxt = (TextView) findViewById(R.id.timestampTxt);
        descriptionTxt = (TextView) findViewById(R.id.descriptionTxt);
        statusTxt = (TextView) findViewById(R.id.statusTxt);
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
        toolbar.setTitle(TITLE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DetailLostActivity.this.onBackPressed();
            }
        });
        //User photo
        new ImageDownloader(userPhotoImg).execute(report.getPhotoUrl());
        //Title
        titleTxt.setText(report.getTitle());
        //Timestamp
        timestampTxt.setText(TimeConvertor.getTimeDifferential(report.getCreated().getValue()));
        //Description
        descriptionTxt.setText(TextTrimmer.trim(report.getDescription()));
        //Status
        statusTxt.setText(report.getFound() ? "Found" : "Not Found");
        statusTxt.setTextColor(report.getFound() ? Color.GREEN : Color.RED);
        //Datetime
        datetimeTxt.setText(TimeConvertor.getDateTime(report.getTimeLost().getValue()));
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
            googleMap.setMyLocationEnabled(true);
            markLocation(report.getLocation());
        }

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

            MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude))
                    .title("Latitude: " + latitude + ", Longitude: " + longitude);

            // adding marker
            googleMap.addMarker(marker);
        } else {
            Log.d(TAG, "No such location");
        }
    }
}

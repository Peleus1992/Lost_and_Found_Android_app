package edu.gatech.wguo64.lostandfoundandroidapp.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.api.client.util.DateTime;

import java.io.IOException;
import java.util.Calendar;

import edu.gatech.wguo64.lostandfoundandroidapp.R;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.GeoPt;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.LostReport;
import edu.gatech.wguo64.lostandfoundandroidapp.constants.Preferences;
import edu.gatech.wguo64.lostandfoundandroidapp.network.Api;
/**
 * Created by guoweidong on 10/25/15.
 */
public class ReportLostActivity extends AppCompatActivity implements GoogleMap.OnMarkerDragListener, GoogleMap.OnMyLocationButtonClickListener, View.OnClickListener {
//    final static int REQUEST_OPEN_CAMERA_FOR_IMAGE = 100;
//    final static int REQUEST_SELECT_PICTURE = 200;
//    final static int REQUEST_POSITION = 300;
    public final static String TAG = ReportLostActivity.class.getName();

    public SharedPreferences preferences;

    Toolbar toolbar;
    EditText titleEdit;
    EditText descriptionEdit;
    CheckBox timeCheck;
    EditText dateEdit;
    EditText timeEdit;
    CheckBox positionCheck;
    MapFragment mapFragment;
    Button changePosBtn;
    Button doneBtn;
    ProgressBar progressBar;

    Calendar datetime;
    GoogleMap googleMap;
    GeoPt geoPt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_lost);

        preferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        inflateViews();
        setUI();
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        geoPt.setLongitude((float) marker.getPosition().longitude);
        geoPt.setLatitude((float)marker.getPosition().latitude);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public boolean onMyLocationButtonClick() {
        markLocation(getCurrentLocation());
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.changePosBtn:

                break;
            case R.id.doneBtn:
                if(!checkForm()) {
                    Snackbar.make(ReportLostActivity.this.findViewById(R.id.coordinatorLayout), "Please fill the blank above", Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }
                submitForm();
                break;
        }
    }

    private void inflateViews() {
        toolbar = (Toolbar)findViewById(R.id.toolBar);
        titleEdit = (EditText)findViewById(R.id.titleEdit);
        descriptionEdit = (EditText)findViewById(R.id.descriptionEdit);
        timeCheck = (CheckBox)findViewById(R.id.timeCheck);
        dateEdit = (EditText)findViewById(R.id.dateEdit);
        timeEdit = (EditText)findViewById(R.id.timeEdit);
        positionCheck = (CheckBox)findViewById(R.id.positionCheck);
        mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        changePosBtn = (Button)findViewById(R.id.changePosBtn);
        doneBtn = (Button)findViewById(R.id.doneBtn);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
    }

    private void setUI() {
        //initiate toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Report Lost");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReportLostActivity.this.onBackPressed();
            }
        });
        //date time
        datetime = Calendar.getInstance();
        dateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                new DatePickerDialog(ReportLostActivity.this, new
                        DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int
                                    monthOfYear, int dayOfMonth) {
                                dateEdit.setText(monthOfYear + "/" +
                                        dayOfMonth + "/"
                                        + year);
                                dateEdit.clearFocus();
                                datetime.set(year, monthOfYear, dayOfMonth);
                            }
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar
                        .MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        timeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                new TimePickerDialog(ReportLostActivity.this, new
                        TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int
                                    hourOfDay, int
                                                          minute) {
                                timeEdit.setText(hourOfDay + ":" + minute);
                                datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                datetime.set(Calendar.MINUTE, minute);
                            }
                        }, calendar.get(Calendar.HOUR_OF_DAY), Calendar
                        .MINUTE, true)
                        .show();
            }
        });
        // position
        googleMap = mapFragment.getMap();

        // check if map is created successfully or not
        if (googleMap == null) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                    .show();
        } else {
            googleMap.setMyLocationEnabled(true);
            googleMap.setOnMyLocationButtonClickListener(this);
            googleMap.setOnMarkerDragListener(this);
            markLocation(getCurrentLocation());
        }
        //Change Position
        changePosBtn.setOnClickListener(this);
        //Done
        doneBtn.setOnClickListener(this);
        //progressBar
        progressBar.setVisibility(View.GONE);
    }

    private void submitForm() {

        LostReport report = new LostReport();

        report.setTitle(titleEdit.getText().toString());

        report.setDescription(descriptionEdit.getText().toString());

        report.setTimeLost(new DateTime(datetime.getTimeInMillis()));

        report.setLocation(geoPt);

        report.setPhotoUrl(preferences.getString(Preferences.ACCOUNT_PHOTO_URL, null));


        /**
         * Use network to post data on server.
         */
        new AsyncTask<LostReport, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressBar.setVisibility(View.VISIBLE);
            }
            @Override
            protected Void doInBackground(LostReport...
                                                  params) {
                try {
                    Api.getClient().lostReport().insert(params[0])
                            .execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                progressBar.setVisibility(View.GONE);
                ReportLostActivity.this.finish();
            }
        }.execute(report);
    }

    private Location getCurrentLocation() {
        // Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Getting Current Location
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TAG", "Getting Current Location Fail");
            return null;
        }
        // get location from network provider or gps provider
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if(location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        return location;
    }

    private void markLocation(Location location) {
        if(location!=null){
            geoPt = new GeoPt();
            geoPt.setLongitude((float)location.getLongitude());
            geoPt.setLatitude((float)location.getLatitude());

            Log.i(TAG, "Location Provider: " + location.getProvider());

            // Getting latitude of the current location
            double latitude = location.getLatitude();

            // Getting longitude of the current location
            double longitude = location.getLongitude();

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

    private boolean checkForm() {
        if(titleEdit.getText() == null || "".equals(titleEdit.getText().toString())) {
            return false;
        }
        if(descriptionEdit.getText() == null || "".equals(descriptionEdit.getText().toString())) {
            return false;
        }
        if(!timeCheck.isChecked() && (dateEdit.getText() == null || "".equals(dateEdit.getText().toString())
        || timeEdit.getText() == null || "".equals(timeEdit.getText().toString()))) {
            return false;
        }
        if(!positionCheck.isChecked() && geoPt == null) {
            return false;
        }
        return true;
    }
}

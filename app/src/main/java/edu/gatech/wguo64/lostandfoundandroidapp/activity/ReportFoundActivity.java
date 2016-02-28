package edu.gatech.wguo64.lostandfoundandroidapp.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.api.client.util.DateTime;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import edu.gatech.wguo64.lostandfoundandroidapp.R;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.FoundReport;
import edu.gatech.wguo64.lostandfoundandroidapp.backend.myApi.model.GeoPt;
import edu.gatech.wguo64.lostandfoundandroidapp.camera.CameraHelper;
import edu.gatech.wguo64.lostandfoundandroidapp.constants.Preferences;
import edu.gatech.wguo64.lostandfoundandroidapp.constants.RequestCodes;
import edu.gatech.wguo64.lostandfoundandroidapp.googlemaps.LocationHelper;
import edu.gatech.wguo64.lostandfoundandroidapp.network.Api;
import edu.gatech.wguo64.lostandfoundandroidapp.utility.ImageConvertor;
import edu.gatech.wguo64.lostandfoundandroidapp.utility.ImageUploader;


/**
 * Created by guoweidong on 10/25/15.
 */
public class ReportFoundActivity extends AppCompatActivity implements GoogleMap.OnMapLongClickListener, GoogleMap.OnMyLocationButtonClickListener, View.OnClickListener {

    public final static String TAG = ReportFoundActivity.class.getName();

    public SharedPreferences preferences;

    Toolbar toolbar;
    EditText titleEdit;
    EditText descriptionEdit;
    ImageView objectImage;
    Button cameraBtn;
    Button selectBtn;
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
    Marker marker;
    GeoPt geoPt;
    String imageName;
    boolean hasImage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_found);

        preferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        inflateViews();
        setUI();
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        geoPt.setLatitude((float)latLng.latitude);
        geoPt.setLongitude((float) latLng.longitude);
        marker.setPosition(latLng);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        marker.remove();
        markLocation(getCurrentLocation());
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cameraBtn:
                imageName = CameraHelper.openCameraForImage
                        (RequestCodes.OPEN_CAMERA_FOR_IMAGE);
                break;
            case R.id.selectBtn:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select " +
                        "Picture"), RequestCodes.SELECT_PICTURE);
                break;
            case R.id.changePosBtn:
                try {

                    startActivityForResult(new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(this), RequestCodes.CHANGE_POSITION);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                    Snackbar.make(ReportFoundActivity.this.findViewById(R.id.coordinatorLayout)
                            , "The google map service is not available now.", Snackbar.LENGTH_SHORT)
                            .show();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                    Snackbar.make(ReportFoundActivity.this.findViewById(R.id.coordinatorLayout)
                            , "The google map service is not available now.", Snackbar.LENGTH_SHORT)
                            .show();
                }
                break;
            case R.id.doneBtn:
                if(!checkForm()) {
                    Snackbar.make(ReportFoundActivity.this.findViewById(R.id.coordinatorLayout), "Please fill the blank above", Snackbar.LENGTH_SHORT)
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
        objectImage = (ImageView)findViewById(R.id.objectImage);
        cameraBtn = (Button)findViewById(R.id.cameraBtn);
        selectBtn = (Button)findViewById(R.id.selectBtn);
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
        toolbar.setTitle("Report Found");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReportFoundActivity.this.onBackPressed();
            }
        });
        //image
        cameraBtn.setOnClickListener(this);
        selectBtn.setOnClickListener(this);
        CameraHelper.init(this);
        //date time
        datetime = Calendar.getInstance();
        dateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                new DatePickerDialog(ReportFoundActivity.this, new
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
                new TimePickerDialog(ReportFoundActivity.this, new
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
            if(checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_GRANTED
                    || checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
            }
            googleMap.setOnMyLocationButtonClickListener(this);
            googleMap.setOnMapLongClickListener(this);
            markLocation(getCurrentLocation());
        }

        changePosBtn.setOnClickListener(this);

        doneBtn.setOnClickListener(this);
        //progressBar
        progressBar.setVisibility(View.GONE);
    }

    private void submitForm() {

        final FoundReport report = new FoundReport();

        report.setTitle(titleEdit.getText().toString());

        report.setDescription(descriptionEdit.getText().toString());

        if(!timeCheck.isChecked()) {
            report.setTimeFound(new DateTime(datetime.getTimeInMillis()));
        }

        if(!positionCheck.isChecked()) {
            report.setLocation(geoPt);
        }

        report.setPhotoUrl(preferences.getString(Preferences.ACCOUNT_PHOTO_URL, null));

        /**
         * Use network to post data on server.
         */
        new AsyncTask<Drawable, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressBar.setVisibility(View.VISIBLE);
            }
            @Override
            protected Void doInBackground(Drawable...
                                                  params) {
                try {
                    if (hasImage) {
                        ImageUploader.ImageInfo imageInfo = ImageUploader.upload(params[0]);
                        report.setImageKey(imageInfo.imageKey);
                        report.setImageURL(imageInfo.imageURL);
                    }
                    Api.getClient().foundReport().insert(report)
                            .execute();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                progressBar.setVisibility(View.GONE);
                ReportFoundActivity.this.finish();
            }
        }.execute(objectImage.getDrawable());
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

            Log.i(TAG, "Location Provider: " + location.getProvider());

            // Getting latitude of the current location
            double latitude = location.getLatitude();

            // Getting longitude of the current location
            double longitude = location.getLongitude();

            // Creating a LatLng object for the current location
            LatLng latLng = new LatLng(latitude, longitude);

            markLocation(latLng);

        } else {
            Log.d(TAG, "No such location");
        }
    }

    private void markLocation(LatLng latLng) {
        if(latLng != null){
            geoPt = new GeoPt();
            geoPt.setLongitude((float) latLng.longitude);
            geoPt.setLatitude((float)latLng.latitude);

            // Showing the current location in Google Map
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            // Zoom in the Google Map
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

            // adding marker
            String markerTitle = LocationHelper.getAddress(this, latLng);
            markerTitle = "".equals(markerTitle) ? "Latitude: " + latLng.latitude
                    + ", Longitude: " + latLng.longitude : markerTitle;
            MarkerOptions markerOptions = new MarkerOptions().position(latLng)
                    .title(markerTitle);

            marker = googleMap.addMarker(markerOptions);

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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent
            data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.OPEN_CAMERA_FOR_IMAGE) {
            if (resultCode == RESULT_OK) {
                File imageFile = CameraHelper.getMediaFile(imageName);
                if (imageFile != null) {
                    Drawable drawable = BitmapDrawable.createFromPath
                            (imageFile.getPath());
                    objectImage.setImageDrawable(drawable);
                    hasImage = true;
                }
            }
        } else if (requestCode == RequestCodes.SELECT_PICTURE) {
            if (resultCode == RESULT_OK) {
                if (data == null) {
                    Log.i("myinfo", "data is empty");
                }
                try {
                    InputStream in = getContentResolver().openInputStream
                            (data.getData());
                    Drawable drawable = BitmapDrawable.createFromStream(in,
                            "image");
                    objectImage.setImageDrawable(drawable);
                    hasImage = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == RequestCodes.CHANGE_POSITION) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i(TAG, "Place: " + place.getName());
                marker.remove();
                markLocation(place.getLatLng());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.d(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // Do nothing
            }
        }
    }
}

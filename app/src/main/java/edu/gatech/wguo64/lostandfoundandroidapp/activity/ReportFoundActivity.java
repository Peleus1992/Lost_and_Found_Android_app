package edu.gatech.wguo64.lostandfoundandroidapp.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TimePicker;

import com.google.api.client.util.DateTime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import edu.gatech.cc.lostandfound.api.lostAndFound.model.FoundReport;
import edu.gatech.cc.lostandfound.api.lostAndFound.model.GeoPt;
import edu.gatech.wguo64.lostandfoundandroidapp.R;
import edu.gatech.wguo64.lostandfoundandroidapp.camera.CameraHelper;
import edu.gatech.wguo64.lostandfoundandroidapp.entity.Position;
import edu.gatech.wguo64.lostandfoundandroidapp.network.Api;
import edu.gatech.wguo64.lostandfoundandroidapp.utility.ImageConvertor;

/**
 * Created by guoweidong on 10/25/15.
 */
public class ReportFoundActivity extends AppCompatActivity {
    public final static String TAG = ReportFoundActivity.class.getName();

    final static int REQUEST_OPEN_CAMERA_FOR_IMAGE = 100;
    final static int REQUEST_SELECT_PICTURE = 200;
    final static int REQUEST_POSITION = 300;
    Toolbar toolbar;
    EditText title;
    EditText description;
    ImageView objectImage;
    Button cameraBtn;
    Button selectBtn;
    EditText dateTxt;
    EditText timeTxt;
    Calendar datetime;
    EditText position;
    Button pinBtn;
    EditText howToGet;
    Button doneBtn;
    ProgressBar progressBar;
    String imageName = null;
    ArrayList<Position> alPositions = new ArrayList<Position>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_found);
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
        setupObject();
        setupImage();
        setupDateTime();
        setupPosition();
        final DateFormat format = new SimpleDateFormat("MM/d/y k:m", Locale
                .ENGLISH);
        doneBtn = (Button) findViewById(R.id.doneBtn);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkForm()) {
                    Snackbar.make(ReportFoundActivity.this.findViewById(R.id.coordinatorLayout), "Please fill the blank above", Snackbar.LENGTH_LONG)
                    .show();
                    return;
                }
                FoundReport report = new FoundReport();
                report.setTitle(title.getText().toString());
                report.setDescription(description.getText().toString());
                report.setTimeFound(new DateTime(datetime.getTimeInMillis()));
                report.setImage(ImageConvertor.drawableToString(objectImage.getBackground()));
                GeoPt geoPt = new GeoPt();
                geoPt.setLatitude(alPositions.get(0).lat);
                geoPt.setLongitude(alPositions.get(0).lng);
                report.setLocation(geoPt);
                /**
                 * Use network to post data on server.
                 */
                new AsyncTask<FoundReport, Void, Void>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected Void doInBackground(FoundReport...
                                                          params) {
                        try {
                            Api.getClient().foundReport().insert(params[0])
                                    .execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(TAG, "doInBackgroud: " + e.getLocalizedMessage());
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        progressBar.setVisibility(View.GONE);
                        ReportFoundActivity.this.finish();
                    }
                }.execute(report);

            }
        });
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
    }
    private boolean checkForm() {
        if(title.getText() == null || "".equals(title.getText().toString())) {
            return false;
        }
        if(description.getText() == null || "".equals(description.getText().toString())) {
            return false;
        }
        if(dateTxt.getText() == null || "".equals(dateTxt.getText().toString())) {
            return false;
        }
        if(timeTxt.getText() == null || "".equals(timeTxt.getText().toString())) {
            return false;
        }
        if(position.getText() == null || "".equals(position.getText().toString())) {
            return false;
        }
        return true;
    }
    public void setupObject() {
        title = (EditText) findViewById(R.id.title);
        description = (EditText) findViewById(R.id.description);
    }

    public void setupImage() {
        objectImage = (ImageView) findViewById(R.id.objectImage);
        cameraBtn = (Button) findViewById(R.id.cameraBtn);
        selectBtn = (Button) findViewById(R.id.selectBtn);
        CameraHelper.init(this);
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageName = CameraHelper.openCameraForImage
                        (REQUEST_OPEN_CAMERA_FOR_IMAGE);
            }
        });
        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select " +
                        "Picture"), REQUEST_SELECT_PICTURE);
            }
        });
    }

    public void setupDateTime() {
        datetime = Calendar.getInstance();
        dateTxt = (EditText) findViewById(R.id.date);
        dateTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                new DatePickerDialog(ReportFoundActivity.this, new
                        DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int
                                    monthOfYear, int dayOfMonth) {
                                dateTxt.setText(monthOfYear + "/" +
                                        dayOfMonth + "/"
                                        + year);
                                dateTxt.clearFocus();
                                datetime.set(year, monthOfYear, dayOfMonth);
                            }
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar
                        .MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        timeTxt = (EditText) findViewById(R.id.time);
        timeTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                new TimePickerDialog(ReportFoundActivity.this, new
                        TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int
                                    hourOfDay, int
                                                          minute) {
                                timeTxt.setText(hourOfDay + ":" + minute);
                                datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                datetime.set(Calendar.MINUTE, minute);
                            }
                        }, calendar.get(Calendar.HOUR_OF_DAY), Calendar
                        .MINUTE, true)
                        .show();
            }
        });

    }

    public void setupPosition() {
        position = (EditText) findViewById(R.id.position);
        pinBtn = (Button)findViewById(R.id.pinBtn);
        pinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReportFoundActivity.this,
                        PinDropActivity.class);
                startActivityForResult(intent, REQUEST_POSITION);
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent
            data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OPEN_CAMERA_FOR_IMAGE) {
            if (resultCode == RESULT_OK) {
                File imageFile = CameraHelper.getMediaFile(imageName);
                if (imageFile != null) {
                    Drawable drawable = BitmapDrawable.createFromPath
                            (imageFile.getPath());
                    objectImage.setBackground(drawable);
                }
            }
        } else if (requestCode == REQUEST_SELECT_PICTURE) {
            if (resultCode == RESULT_OK) {
                if (data == null) {
                    Log.i("myinfo", "data is empty");
                }
                try {
                    InputStream in = getContentResolver().openInputStream
                            (data.getData());
                    Drawable drawable = BitmapDrawable.createFromStream(in,
                            "image");
                    objectImage.setBackground(drawable);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == REQUEST_POSITION) {
            if (resultCode == RESULT_OK) {
                position.setText("");
                alPositions = data.getParcelableArrayListExtra("alPositions");
                for (Position pos : alPositions) {
                    position.append(pos.address + "\n");
                }
            }
        }
    }
}

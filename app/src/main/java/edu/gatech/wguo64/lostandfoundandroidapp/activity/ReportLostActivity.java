package edu.gatech.wguo64.lostandfoundandroidapp.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TimePicker;

import com.google.api.client.util.DateTime;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import edu.gatech.cc.lostandfound.api.lostAndFound.model.GeoPt;
import edu.gatech.cc.lostandfound.api.lostAndFound.model.LostReport;
import edu.gatech.wguo64.lostandfoundandroidapp.R;
import edu.gatech.wguo64.lostandfoundandroidapp.entity.Position;
import edu.gatech.wguo64.lostandfoundandroidapp.network.Api;

/**
 * Created by guoweidong on 10/25/15.
 */
public class ReportLostActivity extends AppCompatActivity {
    final static int REQUEST_OPEN_CAMERA_FOR_IMAGE = 100;
    final static int REQUEST_SELECT_PICTURE = 200;
    final static int REQUEST_POSITION = 300;
    Toolbar toolbar;
    EditText title;
    EditText description;
    EditText date;
    EditText time;
    Calendar datetime;
    EditText position;
    Button pinBtn;
    Button doneBtn;
    ProgressBar progressBar;
    String imageName = null;
    ArrayList<Position> alPositions = new ArrayList<Position>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_lost);
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
        setupObject();
        setupDateTime();
        setupPosition();
        final DateFormat format = new SimpleDateFormat("MM/d/y k:m", Locale
                .ENGLISH);
        doneBtn = (Button) findViewById(R.id.doneBtn);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkForm()) {
                    Snackbar.make(ReportLostActivity.this.findViewById(R.id.coordinatorLayout), "Please fill the blank above", Snackbar.LENGTH_LONG)
                    .show();
                    return;
                }
                LostReport report = new LostReport();
                report.setTitle(title.getText().toString());
                report.setDescription(description.getText().toString());
                List<GeoPt> geoPts = new ArrayList<GeoPt>();

                report.setTimeLost(new DateTime(datetime.getTimeInMillis()));
                for (Position location : alPositions) {
                    GeoPt geoPt = new GeoPt();
                    geoPt.setLatitude(location.lat);
                    geoPt.setLongitude(location.lng);
                    geoPts.add(geoPt);
                }
                report.setLocations(geoPts);

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
        if(date.getText() == null || "".equals(date.getText().toString())) {
            return false;
        }
        if(time.getText() == null || "".equals(time.getText().toString())) {
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



    public void setupDateTime() {
        datetime = Calendar.getInstance();
        date = (EditText) findViewById(R.id.date);
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                new DatePickerDialog(ReportLostActivity.this, new
                        DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int
                                    monthOfYear, int dayOfMonth) {
                                date.setText(monthOfYear + "/" +
                                        dayOfMonth + "/"
                                        + year);
                                date.clearFocus();
                                datetime.set(year, monthOfYear, dayOfMonth);
                            }
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar
                        .MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        time = (EditText) findViewById(R.id.time);
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                new TimePickerDialog(ReportLostActivity.this, new
                        TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int
                                    hourOfDay, int
                                                          minute) {
                                time.setText(hourOfDay + ":" + minute);
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
                Intent intent = new Intent(ReportLostActivity.this,
                        PinDropActivity.class);
                startActivityForResult(intent, REQUEST_POSITION);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent
            data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_POSITION) {
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

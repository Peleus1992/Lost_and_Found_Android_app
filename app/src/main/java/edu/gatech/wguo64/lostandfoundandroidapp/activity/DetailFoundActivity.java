package edu.gatech.wguo64.lostandfoundandroidapp.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.Calendar;

import edu.gatech.cc.lostandfound.api.lostAndFound.model.FoundReport;
import edu.gatech.wguo64.lostandfoundandroidapp.R;
import edu.gatech.wguo64.lostandfoundandroidapp.googlemaps.LocationHelper;
import edu.gatech.wguo64.lostandfoundandroidapp.network.Api;
import edu.gatech.wguo64.lostandfoundandroidapp.time.TimeManager;
import edu.gatech.wguo64.lostandfoundandroidapp.utility.ImageConvertor;

public class DetailFoundActivity extends AppCompatActivity {
    public Toolbar toolbar;

    public ImageView objectImage;
    public TextView title;
    public TextView nickname;
    public TextView timestamp;
    public ImageView emailBtn;
    public TextView returned;
    public TextView description;
    public TextView position;
    public TextView timeFound;

    public ProgressBar progressBar;

    public final static String TITLE = "Detailed Found Report";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_found);
        initToolbar();

        Intent intent = getIntent();
        long reportId = intent.getLongExtra("reportId", -1);
        if(reportId == -1) {
            Log.i("myinfo", "no reportId");
            finish();
        }

        initUI(reportId);

    }

    public void initToolbar() {
        toolbar = (Toolbar)findViewById(R.id.toolBar);
        toolbar.setTitle(TITLE);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DetailFoundActivity.this.onBackPressed();
            }
        });
    }

    public void initUI(long reportId) {
        objectImage = (ImageView) findViewById(R.id.objectImage);
        title = (TextView) findViewById(R.id.title);
        nickname = (TextView) findViewById(R.id.nickname);
        timestamp = (TextView) findViewById(R.id.timestamp);
        emailBtn = (ImageView) findViewById(R.id.emailBtn);
        returned = (TextView) findViewById(R.id.returned);
        description = (TextView) findViewById(R.id.description);
        position = (TextView) findViewById(R.id.position);
        timeFound = (TextView) findViewById(R.id.timeFound);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        new AsyncTask<Long, Void, FoundReport>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected FoundReport doInBackground(Long... params) {
                try {
                    FoundReport foundReport = Api.getClient()
                            .foundReport().get(params[0]).execute();
                    return foundReport;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(final FoundReport report) {
                //handle visibility
                super.onPostExecute(report);

                objectImage.setBackground(report.getImage() != null ? ImageConvertor.stringToDrawable(report.getImage(), false) : getDrawable(R.drawable.img_no_image_found));
                title.setText(report.getTitle());

                nickname.setText(report.getUserNickname());
                timestamp.setText(TimeManager.getTimeDifferential(report.getCreated().getValue()));
                emailBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                "mailto", report.getUserNickname() + "@gmail.com", null));
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                        emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
                        startActivity(Intent.createChooser(emailIntent, "Send email..."));
                    }
                });
                if(report.getReturned()) {
                    returned.setText("Returned");
                    returned.setTextColor(Color.GREEN);
                } else {
                    returned.setText("Not Returned");
                    returned.setTextColor(Color.RED);
                }

                description.setText(report.getDescription());
                position.setText(Html.fromHtml("<b>Positions</b>"));
                position.append("\n" + LocationHelper.getAddress(DetailFoundActivity.this, new LatLng(report.getLocation().getLatitude(), report.getLocation().getLongitude())));
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(report.getTimeFound().getValue());
                timeFound.setText(Html.fromHtml("<b>Date and Time</b>"));
                timeFound.append("\n" + calendar.get(Calendar.MONTH) + "/"
                        + calendar.get(Calendar.DAY_OF_MONTH) + "/"
                        + calendar.get(Calendar.YEAR) + " "
                        + calendar.get(Calendar.HOUR_OF_DAY) + ":"
                        + calendar.get(Calendar.MINUTE));

                progressBar.setVisibility(View.GONE);

            }
        }.execute(reportId);
    }

}

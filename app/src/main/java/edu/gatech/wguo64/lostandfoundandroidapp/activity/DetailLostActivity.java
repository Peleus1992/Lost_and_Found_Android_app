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

import edu.gatech.cc.lostandfound.api.lostAndFound.model.GeoPt;
import edu.gatech.cc.lostandfound.api.lostAndFound.model.LostReport;
import edu.gatech.wguo64.lostandfoundandroidapp.R;
import edu.gatech.wguo64.lostandfoundandroidapp.googlemaps.LocationHelper;
import edu.gatech.wguo64.lostandfoundandroidapp.network.Api;
import edu.gatech.wguo64.lostandfoundandroidapp.time.TimeManager;

public class DetailLostActivity extends AppCompatActivity {

    public Toolbar toolbar;

    public TextView title;
    public TextView nickname;
    public TextView timestamp;
    public ImageView emailBtn;
    public TextView found;

    public TextView description;
    public TextView position;
    public TextView timeLost;

    public ProgressBar progressBar;

    public final static String TITLE = "Detailed Lost Report";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_lost);

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
                DetailLostActivity.this.onBackPressed();
            }
        });
    }

    public void initUI(long reportId) {

        title = (TextView) findViewById(R.id.title);
        nickname = (TextView) findViewById(R.id.nickname);
        timestamp = (TextView) findViewById(R.id.timestamp);
        emailBtn = (ImageView) findViewById(R.id.emailBtn);
        found = (TextView) findViewById(R.id.found);
        description = (TextView) findViewById(R.id.description);
        position = (TextView) findViewById(R.id.position);
        timeLost = (TextView) findViewById(R.id.timeLost);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        new AsyncTask<Long, Void, LostReport>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected LostReport doInBackground(Long... params) {
                try {
                    LostReport lostReport = Api.getClient()
                            .lostReport().get(params[0]).execute();
                    return lostReport;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(final LostReport report) {
                //handle visibility
                super.onPostExecute(report);

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
                if(report.getFound()) {
                    found.setText("Found");
                    found.setTextColor(Color.GREEN);
                } else {
                    found.setText("Not Found");
                    found.setTextColor(Color.RED);
                }

                description.setText(report.getDescription());
                position.setText(Html.fromHtml("<b>Positions</b>"));
                for (GeoPt geoPt : report.getLocations()) {
                    position.append("\n" + LocationHelper.getAddress(DetailLostActivity.this, new LatLng(geoPt.getLatitude(), geoPt.getLongitude())));
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(report.getTimeLost().getValue());
                timeLost.setText(Html.fromHtml("<b>Date and Time</b>"));
                timeLost.append("\n" + calendar.get(Calendar.MONTH) + "/"
                        + calendar.get(Calendar.DAY_OF_MONTH) + "/"
                        + calendar.get(Calendar.YEAR) + " "
                        + calendar.get(Calendar.HOUR_OF_DAY) + ":"
                        + calendar.get(Calendar.MINUTE));

                progressBar.setVisibility(View.GONE);

            }
        }.execute(reportId);
    }

}
